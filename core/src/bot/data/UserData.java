package bot.data;

import javax.swing.Timer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;

import bot.Core;
import bot.command.CommandContext;
import bot.util.POJO;
import bot.util.UsageException;
import bot.util.Utils;
import bot.world.pokemon.EvolutionChain.LevelUpEvolution;
import bot.world.pokemon.Pokemon;
import bot.world.pokemon.Pokemon.CaughtPokemon;
import bot.world.pokemon.Pokemon.SerialPokemon;
import bot.world.pokemon.battle.BattleResult;
import bot.world.pokemon.battle.UserPlayer;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserData {
	
	private static final String USER_DATA_FOLDER = "users/";
	private static final HashMap<Snowflake, UserData> USER_DATA = new HashMap<>();
	
	private static final int REQUEST_TIMEOUT_MS = Utils.minToMs(1);
	
	@Nullable
	public static UserData getData(User user) {
		UserData data = USER_DATA.get(user.getId());
		if(data != null) data.self = user;
		return data;
	}
	@NotNull
	public static UserData reqData(User user) { return reqData(user, true); }
	public static UserData reqData(User user, boolean forSelf) {
		UserData data = getData(user);
		if(data == null)
			throw new UsageException(forSelf ? "You need to pick your starter before you can use this command."
				: "This person has not yet picked their starter."
				);
		return data;
	}
	public static void createData(User user, Pokemon starter) {
		// if(getData(user) != null)
		// 	return false; // already has data
		UserData data = new UserData(user);
		data.self = user;
		data.addPokemon(starter);
		USER_DATA.put(user.getId(), data);
		// return true;
	}
	
	public final Snowflake userId;
	private User self; // populated on first fetch of the data object
	
	// statistics
	private int wins, losses, ties;
	
	// location info
	
	
	// pokemon info
	private CaughtPokemon selectedPokemon;
	private int catchCounter;
	private final TreeMap<Integer, CaughtPokemon> caughtPokemon;
	// private final TreeSet<Integer> encounterDex;
	private final TreeSet<Integer> catchDex;
	
	// TEMP INFO (not saved)
	
	// current battle
	private UserPlayer battlePlayer = null;
	// a request to battle from someone else
	private UserPlayer incomingDuelRequest = null; // stores who it's from
	// a request to battle that this user sent to someone else
	private UserData outgoingDuelRequest = null; // stores who it's for
	private Timer outgoingRequestTimer; // tracks time until outgoing request expires.
	private final Object incomingRequestLock = new Object();
	private final Object outgoingRequestLock = new Object();
	
	// new user
	private UserData(User user) {
		this.userId = user.getId();
		caughtPokemon = new TreeMap<>();
		// encounterDex = new TreeSet<>();
		catchDex = new TreeSet<>();
		catchCounter = 1;
	}
	// existing user
	private UserData(SerialData data) {
		this.userId = Snowflake.of(data.user);
		catchCounter = data.catchCounter;
		wins = data.wins;
		losses = data.losses;
		ties = data.ties;
		
		// encounterDex = new TreeSet<>();
		// for(int dex: data.encounterDex)
		// 	encounterDex.add(dex);
		catchDex = new TreeSet<>();
		for(int dex: data.catchDex)
			catchDex.add(dex);
		
		caughtPokemon = new TreeMap<>();
		for(SerialPokemon p: data.caughtPokemon) {
			CaughtPokemon pokemon = new CaughtPokemon(p, this);
			caughtPokemon.put(pokemon.catchId, pokemon);
			if(p.catchId == data.selectedPokemon)
				selectedPokemon = pokemon;
		}
		
		ensureSelectedPokemon("User "+userId+" was initialized without a valid selected pokemon; setting to starter");
	}
	
	public User getUser() { return self; }
	
	public void buildStatistics(EmbedCreateSpec e) {
		e.setTitle("User Statistics");
		e.setAuthor(self.getUsername(), null, self.getAvatarUrl());
		e.addField("Pokedex completion", catchDex.size()+"/"+DataCore.POKEMON.getSize()+" pokemon caught.", false);
		e.addField("Pokemon currently owned", String.valueOf(caughtPokemon.size()), false);
		
		e.addField("Wins", String.valueOf(wins), true);
		e.addField("Losses", String.valueOf(losses), true);
		e.addField("Ties", String.valueOf(ties), true);
	}
	
	
	// POKEMON MANAGEMENT
	
	
	private void ensureSelectedPokemon() { ensureSelectedPokemon("attempt to fetch selected pokemon of User "+userId+" while selected pokemon is null; setting to starter/first"); }
	private void ensureSelectedPokemon(String onNull) {
		if(selectedPokemon == null) {
			System.err.println(onNull);
			selectedPokemon = caughtPokemon.firstEntry().getValue();
		}
	}
	
	@NotNull
	public CaughtPokemon getSelectedPokemon() {
		ensureSelectedPokemon();
		return selectedPokemon;
	}
	
	public CaughtPokemon selectPokemon(int catchId) {
		CaughtPokemon pokemon = getPokemon(catchId);
		if(pokemon == null)
			throw new UsageException("Could not find pokemon with id "+catchId);
		this.selectedPokemon = pokemon;
		save();
		return pokemon;
	}
	
	// returns the evolved pokemon
	public CaughtPokemon checkEvolution(CaughtPokemon pokemon) {
		LevelUpEvolution evo = pokemon.species.getEvolution();
		if(evo == null || evo.minLevel > pokemon.getLevel()) return null;
		// evolve
		CaughtPokemon evolved = new CaughtPokemon(pokemon, DataCore.POKEMON.get(evo.nextSpeciesDex));
		caughtPokemon.put(evolved.catchId, evolved); // replaces the previous evolution
		catchDex.add(evolved.species.dex);
		if(selectedPokemon == pokemon)
			selectedPokemon = evolved;
		save();
		return evolved;
	}
	
	@Nullable
	public CaughtPokemon getPokemon(int catchId) {
		return caughtPokemon.get(catchId);
	}
	
	public NavigableMap<Integer, CaughtPokemon> getPokemonFrom(int idOffset) {
		return caughtPokemon.tailMap(idOffset, true);
	}
	
	public void addPokemon(Pokemon pokemon) {
		CaughtPokemon caught = new CaughtPokemon(pokemon, this, catchCounter++);
		catchDex.add(caught.species.dex);
		caughtPokemon.put(caught.catchId, caught);
		if(selectedPokemon == null)
			selectedPokemon = caught;
		save();
	}
	
	
	// BATTLE MANAGEMENT
	
	
	public UserPlayer getBattlePlayer() { return battlePlayer; }
	public void setBattlePlayer(@NotNull UserPlayer player) {
		battlePlayer = player;
	}
	public void onBattleEnd(@Nullable BattleResult result) {
		if(result != null) {
			switch(result) {
				case WIN: wins++; break;
				case LOSE: losses++; break;
				case TIE: ties++; break;
			}
		}
		battlePlayer = null;
		
		save();
	}
	
	public void requestDuel(User opponent, CommandContext context) {
		synchronized (outgoingRequestLock) {
			if(outgoingDuelRequest != null) {
				if(outgoingDuelRequest.userId.equals(opponent.getId()))
					throw new UsageException("You have already sent a duel request to " + opponent.getUsername() + ".");
				else
					throw new UsageException("You already have an outgoing duel request with " + outgoingDuelRequest.getUser().getUsername() + "; cancel that one before sending another.");
			}
			
			UserData recipData = getData(opponent);
			boolean available = recipData != null;
			if(available) {
				synchronized (recipData.incomingRequestLock) {
					if(recipData.incomingDuelRequest != null || recipData.battlePlayer != null)
						available = false;
					else
						recipData.incomingDuelRequest = new UserPlayer(context.channel, this, selectedPokemon);
				}
			}
			if(!available)
				throw new UsageException(opponent.getUsername()+" is not currently available for dueling. They are either in a battle, already have a duel request, or don't yet have a pokemon.");
			
			outgoingDuelRequest = recipData;
			Timer t = new Timer(REQUEST_TIMEOUT_MS, e -> {
				synchronized (outgoingRequestLock) {
					if(outgoingRequestTimer == null)
						return; // was already handled
					clearOutgoingRequest();
				}
				context.channel.createMessage("Duel request against " + opponent.getUsername() + " timed out. Send another request if you wish to start a duel.")
					.flatMap(msg -> opponent.getPrivateChannel())
					.flatMap(channel -> channel.createMessage("Duel request from " + self.getUsername() + " timed out."))
					.subscribe();
			});
			outgoingRequestTimer = t;
			t.start();
		}
	}
	
	public UserPlayer clearIncomingRequest() { return clearIncomingRequest(true); }
	private UserPlayer clearIncomingRequest(boolean checkOther) {
		synchronized (incomingRequestLock) {
			if(incomingDuelRequest == null)
				throw new UsageException("No pending requests.");
			
			UserPlayer opponent = incomingDuelRequest;
			incomingDuelRequest = null;
			if(checkOther)
				opponent.data.clearOutgoingRequest(false);
			return opponent;
		}
	}
	public UserData clearOutgoingRequest() { return clearOutgoingRequest(true); }
	private UserData clearOutgoingRequest(boolean checkOther) {
		synchronized (outgoingRequestLock) {
			if(outgoingDuelRequest == null)
				throw new UsageException("No outgoing requests.");
			
			outgoingRequestTimer.stop();
			outgoingRequestTimer = null;
			
			UserData opponentData = outgoingDuelRequest;
			outgoingDuelRequest = null;
			
			if(checkOther)
				opponentData.clearIncomingRequest(false);
			
			return opponentData;
		}
	}
	
	
	public void save() {
		SerialData data = new SerialData(this);
		try {
			dataWriter.writeValue(new File(USER_DATA_FOLDER+userId.asLong()+".json"), data);
		} catch(IOException e) {
			System.err.println("error saving data to file for user "+userId);
			e.printStackTrace();
		}
	}
	
	@POJO
	private static class SerialData {
		public long user;
		public int wins, losses, ties;
		public int selectedPokemon;
		public int catchCounter;
		public SerialPokemon[] caughtPokemon;
		// public int[] encounterDex;
		public int[] catchDex;
		
		public SerialData() {}
		public SerialData(UserData data) {
			user = data.userId.asLong();
			wins = data.wins;
			ties = data.ties;
			losses = data.losses;
			selectedPokemon = data.selectedPokemon.catchId;
			catchCounter = data.catchCounter;
			// encounterDex = Utils.map(int[].class, data.encounterDex, dex -> dex);
			catchDex = Utils.map(int[].class, data.catchDex, dex -> dex);
			caughtPokemon = Utils.map(SerialPokemon[].class, data.caughtPokemon.values(), SerialPokemon::new);
		}
	}
	
	private static final ObjectReader dataReader = Core.jsonMapper.readerFor(SerialData.class);
	private static final ObjectWriter dataWriter = Core.jsonMapper.writerFor(SerialData.class);
	
	public static void load() throws IOException {
		System.out.println("loading user data...");
		Files.list(new File("users/").toPath()).forEach(path -> {
			SerialData serialData;
			try {
				serialData = dataReader.readValue(path.toFile());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
			UserData data = new UserData(serialData);
			USER_DATA.put(data.userId, data);
		});
		System.out.println("loaded data for "+ Utils.plural(USER_DATA.size(), "user"));
	}
}
