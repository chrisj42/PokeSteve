package bot.world.pokemon.move;

import java.util.function.Consumer;
import java.util.function.Function;

import bot.data.json.MissingPropertyException;
import bot.data.json.NodeParser;
import bot.data.json.node.JsonArrayNode;
import bot.data.json.node.JsonObjectNode;
import bot.world.pokemon.DamageCategory;
import bot.data.DataCore;
import bot.world.pokemon.Stat;
import bot.world.pokemon.Type;
import bot.world.pokemon.battle.MoveContext;
import bot.world.pokemon.battle.status.StatusEffect;
import bot.world.pokemon.move.DamageCalculator.ClassicDamage;
import bot.world.pokemon.move.DamageCalculator.PercentageDamage;
import bot.world.pokemon.move.DamageProperty.DamageBuilder;
import bot.world.pokemon.move.EffectGroup.EffectGroupBuilder;
import bot.world.pokemon.move.PokemonEffectSet.PokemonEffect;
import bot.world.pokemon.move.StatProperty.StatBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import org.jetbrains.annotations.Nullable;

public enum Moves {
	
	/*
		pp,
	type,
	damage category,
	power,
	etc common things are covered by json
		
		forms of effect:
		- damage:
			- normal
				- crits,
	hits,
			- percentage
			- fixed
			- regen
			- recoil 
		
		- stat stage
		
		- status effect
		
		- weather effect
	 */
	
	// gen 1
	Pound,
	Karate_Chop,
	Double_Slap(m -> m.damageBuilder.multiHit(MultiHitProperty.SCALED_2_5)),
	Comet_Punch(m -> m.damageBuilder.multiHit(MultiHitProperty.SCALED_2_5)),
	Mega_Punch,
	Pay_Day,
	Fire_Punch(new MoveBuilder()
		.secondary(10)
		.affectEnemy(new StatusProperty(StatusEffect.Burn))
		.add()
	),
	Ice_Punch(new MoveBuilder()
		.secondary(10)
		.affectEnemy(new StatusProperty(StatusEffect.Freeze))
		.add()
	),
	Thunder_Punch(new MoveBuilder()
		.secondary(10)
		.affectEnemy(new StatusProperty(StatusEffect.Paralysis))
		.add()
	),
	Scratch,
	Vice_Grip,
	Guillotine(new MoveBuilder()
		.ohko(DamageCategory.Physical)
	),
	Razor_Wind(m -> m.builder.charge(ChargeState.Normal)),
	Swords_Dance(new MoveBuilder()
		.primary().affectSelf(StatBuilder.get(Stat.Attack, 2)).add()
	),
	Cut,
	Gust(new MoveBuilder()
		.damage(new DamageBuilder(DamageCategory.Special, new ClassicDamage(40, 0) {
			@Override
			protected int getPower(MoveContext context) {
				int base = super.getPower(context);
				if(context.user.getChargeState() == ChargeState.Sky)
					base *= 2; // power doubled in the sky
				return base;
			}
		}))
	),
	Wing_Attack,
	Whirlwind, // no impl
	Fly(m -> m.builder.charge(ChargeState.Sky)),
	Bind(new MoveBuilder()
		.primary().affectEnemy(new TrapProperty(p -> p+" is squeezed by "+p.getOpponent()+"!")).add()
	),
	Slam,
	Vine_Whip,
	Stomp(new MoveBuilder()
		.secondary(30).affectEnemy(PokemonEffect.FLINCH).add()
	),
	Double_Kick(
		m -> m.damageBuilder.multiHit(new MultiHitProperty(2))
	),
	Mega_Kick,
	Jump_Kick(new MoveBuilder().onMiss(context -> {
		int dam = context.user.alterHealth(-context.userPokemon.getStat(Stat.Health) / 2);
		context.withUser("crashed to the ground and took ").append(-dam).append(" damage!");
	})),
	Rolling_Kick(new MoveBuilder()
		.secondary(30).affectEnemy(PokemonEffect.FLINCH).add()
	),
	Sand_Attack(new MoveBuilder()
		.primary().affectEnemy(StatBuilder.get(Stat.Accuracy, -1)).add()
	),
	Headbutt(new MoveBuilder()
		.secondary(30).affectEnemy(PokemonEffect.FLINCH).add()
	),
	Horn_Attack,
	Fury_Attack(m -> m.damageBuilder.multiHit(MultiHitProperty.SCALED_2_5)),
	Horn_Drill(new MoveBuilder().ohko(DamageCategory.Physical)),
	Tackle,
	Body_Slam(new MoveBuilder()
		.secondary(30).affectEnemy(new StatusProperty(StatusEffect.Paralysis)).add()
	),
	Wrap(new MoveBuilder()
		.primary().affectEnemy(new TrapProperty(p -> p+" was wrapped by "+p.getOpponent()+"!")).add()
	),
	Take_Down(m -> m.damageBuilder.recoil(25)),
	Thrash(new MoveBuilder().primary().affectSelf(PokemonEffect.THRASH).add()),
	Double_Edge(m -> m.damageBuilder.recoil(33)),
	Tail_Whip(new MoveBuilder()
		.primary().affectEnemy(StatBuilder.get(Stat.Defense, -1)).add()
	),
	Poison_Sting(new MoveBuilder()
		.secondary(30).affectEnemy(new StatusProperty(StatusEffect.Poison)).add()
	),
	Twineedle(new MoveBuilder() // TODO each hit should have a chance to activate the secondary effects
		.secondary(20).affectEnemy(new StatusProperty(StatusEffect.Poison)).add(),
		m -> m.damageBuilder.multiHit(new MultiHitProperty(2))
	),
	Pin_Missile(m -> m.damageBuilder.multiHit(MultiHitProperty.SCALED_2_5)),
	Leer(new MoveBuilder()
		.primary().affectEnemy(StatBuilder.get(Stat.Defense, -1)).add()
	),
	Bite(new MoveBuilder()
		.secondary(30).affectEnemy(PokemonEffect.FLINCH).add()
	),
	Growl(new MoveBuilder()
		.primary().affectEnemy(StatBuilder.get(Stat.Attack, -1)).add()
	),
	Roar, // not impl
	Sing(new MoveBuilder()
		.primary().affectEnemy(new StatusProperty(StatusEffect.Sleep)).add()
	),
	Supersonic(new MoveBuilder()
		.primary().affectEnemy(new StatusProperty(StatusEffect.Confusion)).add()
	),
	Sonic_Boom(new MoveBuilder().damage(
		new DamageBuilder(DamageCategory.Special, (context, damageType) -> 20)
	)),
	Disable(new MoveBuilder()
		.primary().affectEnemy(PokemonEffect.DISABLE).add()
	),
	Acid,
	Ember,
	Flamethrower,
	Mist,
	Water_Gun,
	Hydro_Pump,
	Surf,
	Ice_Beam,
	Blizzard,
	Psybeam,
	Bubble_Beam,
	Aurora_Beam,
	Hyper_Beam,
	Peck,
	Drill_Peck,
	Submission,
	Low_Kick,
	Counter,
	Seismic_Toss,
	Strength,
	Absorb,
	Mega_Drain,
	Leech_Seed,
	Growth,
	Razor_Leaf,
	Solar_Beam,
	Poison_Powder,
	Stun_Spore,
	Sleep_Powder,
	Petal_Dance,
	String_Shot,
	Dragon_Rage,
	Fire_Spin,
	Thunder_Shock,
	Thunderbolt,
	Thunder_Wave,
	Thunder,
	Rock_Throw,
	Earthquake,
	Fissure,
	Dig,
	Toxic,
	Confusion,
	Psychic,
	Hypnosis,
	Meditate,
	Agility,
	Quick_Attack,
	Rage,
	Teleport,
	Night_Shade,
	Mimic,
	Screech,
	Double_Team,
	Recover,
	Harden,
	Minimize,
	Smokescreen,
	Confuse_Ray,
	Withdraw,
	Defense_Curl,
	Barrier,
	Light_Screen,
	Haze,
	Reflect,
	Focus_Energy,
	Bide,
	Metronome,
	Mirror_Move,
	Self_Destruct,
	Egg_Bomb,
	Lick,
	Smog,
	Sludge,
	Bone_Club,
	Fire_Blast,
	Waterfall,
	Clamp,
	Swift,
	Skull_Bash,
	Spike_Cannon,
	Constrict,
	Amnesia,
	Kinesis,
	Soft_Boiled,
	High_Jump_Kick,
	Glare,
	Dream_Eater,
	Poison_Gas,
	Barrage,
	Leech_Life,
	Lovely_Kiss,
	Sky_Attack,
	Transform,
	Bubble,
	Dizzy_Punch,
	Spore,
	Flash,
	Psywave,
	Splash,
	Acid_Armor,
	Crabhammer,
	Explosion,
	Fury_Swipes,
	Bonemerang,
	Rest,
	Rock_Slide,
	Hyper_Fang,
	Sharpen,
	Conversion,
	Tri_Attack,
	Super_Fang,
	Slash,
	Substitute,
	Struggle,
	
	// gen 2
	Sketch,
	Triple_Kick,
	Thief,
	Spider_Web,
	Mind_Reader,
	Nightmare,
	Flame_Wheel,
	Snore,
	Curse,
	Flail,
	Conversion_2,
	Aeroblast,
	Cotton_Spore,
	Reversal,
	Spite,
	Powder_Snow,
	Protect,
	Mach_Punch,
	Scary_Face,
	Feint_Attack,
	Sweet_Kiss,
	Belly_Drum,
	Sludge_Bomb,
	Mud_Slap,
	Octazooka,
	Spikes,
	Zap_Cannon,
	Foresight,
	Destiny_Bond,
	Perish_Song,
	Icy_Wind,
	Detect,
	Bone_Rush,
	Lock_On,
	Outrage,
	Sandstorm,
	Giga_Drain,
	Endure,
	Charm,
	Rollout,
	False_Swipe,
	Swagger,
	Milk_Drink,
	Spark,
	Fury_Cutter,
	Steel_Wing,
	Mean_Look,
	Attract,
	Sleep_Talk,
	Heal_Bell,
	Return,
	Present,
	Frustration,
	Safeguard,
	Pain_Split,
	Sacred_Fire,
	Magnitude,
	Dynamic_Punch,
	Megahorn,
	Dragon_Breath,
	Baton_Pass,
	Encore,
	Pursuit,
	Rapid_Spin,
	Sweet_Scent,
	Iron_Tail,
	Metal_Claw,
	Vital_Throw,
	Morning_Sun,
	Synthesis,
	Moonlight,
	Hidden_Power,
	Cross_Chop,
	Twister,
	Rain_Dance,
	Sunny_Day,
	Crunch,
	Mirror_Coat,
	Psych_Up,
	Extreme_Speed,
	Ancient_Power,
	Shadow_Ball,
	Future_Sight,
	Rock_Smash,
	Whirlpool,
	Beat_Up,
	
	// gen 3
	Fake_Out,
	Uproar,
	Stockpile,
	Spit_Up,
	Swallow,
	Heat_Wave,
	Hail,
	Torment,
	Flatter,
	Will_O_Wisp("Will-O-Wisp"),
	Memento,
	Facade,
	Focus_Punch,
	Smelling_Salts,
	Follow_Me,
	Nature_Power,
	Charge,
	Taunt,
	Helping_Hand,
	Trick,
	Role_Play,
	Wish,
	Assist,
	Ingrain,
	Superpower,
	Magic_Coat,
	Recycle,
	Revenge,
	Brick_Break,
	Yawn,
	Knock_Off,
	Endeavor,
	Eruption,
	Skill_Swap,
	Imprison,
	Refresh,
	Grudge,
	Snatch,
	Secret_Power,
	Dive,
	Arm_Thrust,
	Camouflage,
	Tail_Glow,
	Luster_Purge,
	Mist_Ball,
	Feather_Dance,
	Teeter_Dance,
	Blaze_Kick,
	Mud_Sport,
	Ice_Ball,
	Needle_Arm,
	Slack_Off,
	Hyper_Voice,
	Poison_Fang,
	Crush_Claw,
	Blast_Burn,
	Hydro_Cannon,
	Meteor_Mash,
	Astonish,
	Weather_Ball,
	Aromatherapy,
	Fake_Tears,
	Air_Cutter,
	Overheat,
	Odor_Sleuth,
	Rock_Tomb,
	Silver_Wind,
	Metal_Sound,
	Grass_Whistle,
	Tickle,
	Cosmic_Power,
	Water_Spout,
	Signal_Beam,
	Shadow_Punch,
	Extrasensory,
	Sky_Uppercut,
	Sand_Tomb,
	Sheer_Cold,
	Muddy_Water,
	Bullet_Seed,
	Aerial_Ace,
	Icicle_Spear,
	Iron_Defense,
	Block,
	Howl,
	Dragon_Claw,
	Frenzy_Plant,
	Bulk_Up,
	Bounce,
	Mud_Shot,
	Poison_Tail,
	Covet,
	Volt_Tackle,
	Magical_Leaf,
	Water_Sport,
	Calm_Mind,
	Leaf_Blade,
	Dragon_Dance,
	Rock_Blast,
	Shock_Wave,
	Water_Pulse,
	Doom_Desire,
	Psycho_Boost,
	
	// gen 4
	Roost,
	Gravity,
	Miracle_Eye,
	Wake_Up_Slap("Wake-up Slap"),
	Hammer_Arm,
	Gyro_Ball,
	Healing_Wish,
	Brine,
	Natural_Gift,
	Feint,
	Pluck,
	Tailwind,
	Acupressure,
	Metal_Burst,
	U_Turn,
	Close_Combat,
	Payback,
	Assurance,
	Embargo,
	Fling,
	Psycho_Shift,
	Trump_Card,
	Heal_Block,
	Wring_Out,
	Power_Trick,
	Gastro_Acid,
	Lucky_Chant,
	Me_First,
	Copycat,
	Power_Swap,
	Guard_Swap,
	Punishment,
	Last_Resort,
	Worry_Seed,
	Sucker_Punch,
	Toxic_Spikes,
	Heart_Swap,
	Aqua_Ring,
	Magnet_Rise,
	Flare_Blitz,
	Force_Palm,
	Aura_Sphere,
	Rock_Polish,
	Poison_Jab,
	Dark_Pulse,
	Night_Slash,
	Aqua_Tail,
	Seed_Bomb,
	Air_Slash,
	X_Scissor,
	Bug_Buzz,
	Dragon_Pulse,
	Dragon_Rush,
	Power_Gem,
	Drain_Punch,
	Vacuum_Wave,
	Focus_Blast,
	Energy_Ball,
	Brave_Bird,
	Earth_Power,
	Switcheroo,
	Giga_Impact,
	Nasty_Plot,
	Bullet_Punch,
	Avalanche,
	Ice_Shard,
	Shadow_Claw,
	Thunder_Fang,
	Ice_Fang,
	Fire_Fang,
	Shadow_Sneak,
	Mud_Bomb,
	Psycho_Cut,
	Zen_Headbutt,
	Mirror_Shot,
	Flash_Cannon,
	Rock_Climb,
	Defog,
	Trick_Room,
	Draco_Meteor,
	Discharge,
	Lava_Plume,
	Leaf_Storm,
	Power_Whip,
	Rock_Wrecker,
	Cross_Poison,
	Gunk_Shot,
	Iron_Head,
	Magnet_Bomb,
	Stone_Edge,
	Captivate,
	Stealth_Rock,
	Grass_Knot,
	Chatter,
	Judgment,
	Bug_Bite,
	Charge_Beam,
	Wood_Hammer,
	Aqua_Jet,
	Attack_Order,
	Defend_Order,
	Heal_Order,
	Head_Smash,
	Double_Hit,
	Roar_Of_time,
	Spacial_Rend,
	Lunar_Dance,
	Crush_Grip,
	Magma_Storm,
	Dark_Void,
	Seed_Flare,
	Ominous_Wind,
	Shadow_Force,
	
	// gen 5
	Hone_Claws,
	Wide_Guard,
	Guard_Split,
	Power_Split,
	Wonder_Room,
	Psyshock,
	Venoshock,
	Autotomize,
	Rage_Powder,
	Telekinesis,
	Magic_Room,
	Smack_Down,
	Storm_Throw,
	Flame_Burst,
	Sludge_Wave,
	Quiver_Dance,
	Heavy_Slam,
	Synchronoise,
	Electro_Ball,
	Soak,
	Flame_Charge,
	Coil,
	Low_Sweep,
	Acid_Spray,
	Foul_Play,
	Simple_Beam,
	Entrainment,
	After_You,
	Round,
	Echoed_Voice,
	Chip_Away,
	Clear_Smog,
	Stored_Power,
	Quick_Guard,
	Ally_Switch,
	Scald,
	Shell_Smash,
	Heal_Pulse,
	Hex,
	Sky_Drop,
	Shift_Gear,
	Circle_Throw,
	Incinerate,
	Quash,
	Acrobatics,
	Reflect_Type,
	Retaliate,
	Final_Gambit,
	Bestow,
	Inferno,
	Water_Pledge,
	Fire_Pledge,
	Grass_Pledge,
	Volt_Switch,
	Struggle_Bug,
	Bulldoze,
	Frost_Breath,
	Dragon_Tail,
	Work_Up,
	Electroweb,
	Wild_Charge,
	Drill_Run,
	Dual_Chop,
	Heart_Stamp,
	Horn_Leech,
	Sacred_Sword,
	Razor_Shell,
	Heat_Crash,
	Leaf_Tornado,
	Steamroller,
	Cotton_Guard,
	Night_Daze,
	Psystrike,
	Tail_Slap,
	Hurricane,
	Head_Charge,
	Gear_Grind,
	Searing_Shot,
	Techno_Blast,
	Relic_Song,
	Secret_Sword,
	Glaciate,
	Bolt_Strike,
	Blue_Flare,
	Fiery_Dance,
	Freeze_Shock,
	Ice_Burn,
	Snarl,
	Icicle_Crash,
	V_Create,
	Fusion_Flare,
	Fusion_Bolt,
	
	// gen 6 p1
	Flying_Press,
	Mat_Block,
	Belch,
	Rototiller,
	Sticky_Web,
	Fell_Stinger,
	Phantom_Force,
	Trick_or_Treat,
	Noble_Roar,
	Ion_Deluge,
	Parabolic_Charge,
	Forests_Curse,
	Petal_Blizzard,
	Freeze_Dry,
	Disarming_Voice,
	Parting_Shot,
	Topsy_Turvy,
	Draining_Kiss,
	Crafty_Shield,
	Flower_Shield,
	Grassy_Terrain,
	Misty_Terrain,
	Electrify,
	Play_Rough,
	Fairy_Wind,
	Moonblast,
	Boomburst,
	Fairy_Lock,
	Kings_Shield,
	Play_Nice,
	Confide,
	Diamond_Storm,
	Steam_Eruption,
	Hyperspace_Hole,
	Water_Shuriken,
	Mystical_Fire,
	Spiky_Shield,
	Aromatic_Mist,
	Eerie_Impulse,
	Venom_Drench,
	Powder,
	Geomancy,
	Magnetic_Flux,
	Happy_Hour,
	Electric_Terrain,
	Dazzling_Gleam,
	Celebrate,
	Hold_Hands,
	Baby_Doll_Eyes,
	Nuzzle,
	Hold_Back,
	Infestation,
	Power_Up_Punch("Power-up Punch"),
	Oblivion_Wing,
	Thousand_Arrows,
	Thousand_Waves,
	Lands_Wrath("Land's Wrath"),
	Light_of_Ruin,
	
	// gen 6 p2 (omega ruby / alpha sapphire
	Origin_Pulse,
	Precipice_Blades,
	Dragon_Ascent,
	Hyperspace_Fury,
	
	// haven't checked names past here
	
	// gen 7 p1
	Breakneck_Blitz__physical,
	Breakneck_Blitz__special,
	All_Out_pummeling__physical,
	All_Out_pummeling__special,
	Supersonic_Skystrike__physical,
	Supersonic_Skystrike__special,
	Acid_Downpour__physical,
	Acid_Downpour__special,
	Tectonic_Rage__physical,
	Tectonic_Rage__special,
	Continental_Crush__physical,
	Continental_Crush__special,
	Savage_Spin_out__physical,
	Savage_Spin_out__special,
	Never_Ending_nightmare__physical,
	Never_Ending_nightmare__special,
	Corkscrew_Crash__physical,
	Corkscrew_Crash__special,
	Inferno_Overdrive__physical,
	Inferno_Overdrive__special,
	Hydro_Vortex__physical,
	Hydro_Vortex__special,
	Bloom_Doom__physical,
	Bloom_Doom__special,
	Gigavolt_Havoc__physical,
	Gigavolt_Havoc__special,
	Shattered_Psyche__physical,
	Shattered_Psyche__special,
	Subzero_Slammer__physical,
	Subzero_Slammer__special,
	Devastating_Drake__physical,
	Devastating_Drake__special,
	Black_Hole_eclipse__physical,
	Black_Hole_eclipse__special,
	Twinkle_Tackle__physical,
	Twinkle_Tackle__special,
	Catastropika,
	Shore_Up,
	First_Impression,
	Baneful_Bunker,
	Spirit_Shackle,
	Darkest_Lariat,
	Sparkling_Aria,
	Ice_Hammer,
	Floral_Healing,
	High_Horsepower,
	Strength_Sap,
	Solar_Blade,
	Leafage,
	Spotlight,
	Toxic_Thread,
	Laser_Focus,
	Gear_Up,
	Throat_Chop,
	Pollen_Puff,
	Anchor_Shot,
	Psychic_Terrain,
	Lunge,
	Fire_Lash,
	Power_Trip,
	Burn_Up,
	Speed_Swap,
	Smart_Strike,
	Purify,
	Revelation_Dance,
	Core_Enforcer,
	Trop_Kick,
	Instruct,
	Beak_Blast,
	Clanging_Scales,
	Dragon_Hammer,
	Brutal_Swing,
	Aurora_Veil,
	Sinister_Arrow_raid,
	Malicious_Moonsault,
	Oceanic_Operetta,
	Guardian_Of_Alola,
	Soul_Stealing_7_Star_Strike,
	Stoked_Sparksurfer,
	Pulverizing_Pancake,
	Extreme_Evoboost,
	Genesis_Supernova,
	Shell_Trap,
	Fleur_Cannon,
	Psychic_Fangs,
	Stomping_Tantrum,
	Shadow_Bone,
	Accelerock,
	Liquidation,
	Prismatic_Laser,
	Spectral_Thief,
	Sunsteel_Strike,
	Moongeist_Beam,
	Tearful_Look,
	Zing_Zap,
	Natures_Madness,
	Multi_Attack,
	_10_000_000_Volt_Thunderbolt("10,000,000 Volt Thunderbolt"),
	
	// gen 7 p2 (ultra sun / ultra moon)
	Mind_Blown,
	Plasma_Fists,
	Photon_Geyser,
	Light_That_burns_the_sky,
	Searing_Sunraze_smash,
	Menacing_Moonraze_maelstrom,
	Lets_Snuggle_forever,
	Splintered_Stormshards,
	Clangorous_Soulblaze,
	Zippy_Zap,
	Splishy_Splash,
	Floaty_Fall,
	Pika_Papow,
	Bouncy_Bubble,
	Buzzy_Buzz,
	Sizzly_Slide,
	Glitzy_Glow,
	Baddy_Bad,
	Sappy_Seed,
	Freezy_Frost,
	Sparkly_Swirl,
	Veevee_Volley,
	Double_Iron_Bash,
	
	// gen 8
	Max_Guard,
	Dynamax_Cannon,
	Snipe_Shot,
	Jaw_Lock,
	Stuff_Cheeks,
	No_Retreat,
	Tar_Shot,
	Magic_Powder,
	Dragon_Darts,
	Teatime,
	Octolock,
	Bolt_Beak,
	Fishious_Rend,
	Court_Change,
	Max_Flare,
	Max_Flutterby,
	Max_Lightning,
	Max_Strike,
	Max_Knuckle,
	Max_Phantasm,
	Max_Hailstorm,
	Max_Ooze,
	Max_Geyser,
	Max_Airstream,
	Max_Starfall,
	Max_Wyrmwind,
	Max_Mindstorm,
	Max_Rockfall,
	Max_Quake,
	Max_Darkness,
	Max_Overgrowth,
	Max_Steelspike,
	Clangorous_Soul,
	Body_Press,
	Decorate,
	Drum_Beating,
	Snap_Trap,
	Pyro_Ball,
	Behemoth_Blade,
	Behemoth_Bash,
	Aura_Wheel,
	Breaking_Swipe,
	Branch_Poke,
	Overdrive,
	Apple_Acid,
	Grav_Apple,
	Spirit_Break,
	Strange_Steam,
	Life_Dew,
	Obstruct,
	False_Surrender,
	Meteor_Assault,
	Eternabeam,
	Steel_Beam;
	
	public static final Moves[] values = Moves.values();
	
	// private final String name;
	private MoveDescription description;
	private int accuracy;
	private int pp;
	private Type type;
	private int priority;
	private DamageProperty classicDamage;
	private DamageBuilder damageBuilder;
	private int secondaryChance;
	// private EffectGroupBuilder secondary;
	
	private Move move;
	private final MoveBuilder builder;
	private final Consumer<Moves> valueEditor;
	
	Moves() { this((String)null); }
	Moves(String name) { this(new MoveBuilder(name)); }
	Moves(MoveBuilder b) { this(b, m -> {}); }
	Moves(Consumer<Moves> valueEditor) { this(new MoveBuilder(), valueEditor); }
	Moves(MoveBuilder b, Consumer<Moves> valueEditor) {
		/*if(b == null) {
			move = null;
			return;
		}*/
		this.builder = b;
		this.valueEditor = valueEditor;
		
		if(ordinal() >= DataCore.MOVE_JSON.getLength())
			return;
		
		try {
			final JsonObjectNode node = DataCore.MOVE_JSON.getObjectNode(ordinal());
			final JsonObjectNode meta = node.getObjectNode("meta");
			
			description = new MoveDescription(node);
			int typeId = NodeParser.getResourceId(node.getObjectNode("type"))-1;
			type = typeId >= 0 ? Type.values[typeId] : null;
			pp = node.parseValueNode("pp", JsonNode::intValue);
			priority = node.parseValueNode("priority", JsonNode::intValue);
			accuracy = node.parseValueNode("accuracy", JsonNode::intValue);
			
			int damageTypeId = NodeParser.getResourceId(node.getObjectNode("damage_class")) - 2;
			DamageCategory damageType = damageTypeId >= 0 ? DamageCategory.values[damageTypeId] : null;
			if(damageType == null)
				classicDamage = DamageProperty.NO_DAMAGE;
			else {
				int power = node.parseValueNode("power", JsonNode::intValue);
				int critRateBonus = meta.parseValueNode("crit_rate", JsonNode::intValue);
				damageBuilder = new DamageBuilder(damageType, new ClassicDamage(power, critRateBonus));
				// check for stat changes
				// actually don't because the database is simply too unreliable
				// JsonArrayNode statChanges = node.getArrayNode("stat_changes");
			}
			
			// secondaryChance = node.parseValueNode("effect_chance", JsonNode::intValue);
			
			// EffectGroupBuilder effects = new EffectGroupBuilder(b, false);
			
			/*JsonArrayNode statChanges = node.getArrayNode("stat_changes");
			if(statChanges.getLength() > 0) {
				StatBuilder builder = new StatBuilder();
				for(int i = 0; i < statChanges.getLength(); i++) {
					JsonObjectNode change = statChanges.getObjectNode(i);
					Stat stat = Stat.values[NodeParser.getResourceId(change.getObjectNode("stat")) - 1];
					int amount = change.parseValueNode("change", JsonNode::intValue);
					builder.set(stat, amount);
				}
				builder.create();
			}*/
			/*target = MoveTarget.getTarget(node.getObjectNode("target").parseValueNode("name", JsonNode::textValue));
			effectChance = node.parseValueNode("effect_chance", JsonNode::intValue);
			
			// damage = new DamageEffect();
			stat = new StatEffect(node, meta);
			status = new ApplyStatusEffect(node, meta);
			
			drain = meta.parseValueNode("drain", JsonNode::intValue);
			healing = meta.parseValueNode("healing", JsonNode::intValue);
			flinchChance = meta.parseValueNode("flinch_chance",JsonNode::intValue);*/
		} catch(MissingPropertyException e) {
			System.err.println("error while parsing json data for move "+name());
			e.printStackTrace();
		}
		
		// this.move = b.create(this);
	}
	
	public Move getMove() {
		if(move == null)
			throw new IllegalStateException("attempted to fetch move object from "+this+" before it was initialized.");
		return move;
	}
	
	// post-enum def initialization
	static {
		for(Moves move: Moves.values) {
			move.valueEditor.accept(move);
			if(move.classicDamage == null && move.damageBuilder != null)
				move.classicDamage = move.damageBuilder.create();
			move.move = move.builder.create(move);
		}
	}
	
	static class MoveBuilder {
		
		private final String name;
		private ChargeState doesCharge;
		private Boolean doesRecharge;
		private AccuracyProperty accuracyProp;
		private DamageBuilder damageEffect;
		private EffectGroupBuilder primary;
		private EffectGroupBuilder secondary;
		private Integer secondaryChance;
		private Function<MoveContext, Boolean> moveCondition;
		private Consumer<MoveContext> onMoveMiss;
		
		// MoveBuilder(Type type, int pp) {  this(null, type, pp); }
		// MoveBuilder(Type type, int pp, int accuracy) {  this(null, type, pp, accuracy); }
		// MoveBuilder(@Nullable String name, Type type, int pp) { this(name, type, pp, 0); }
		// MoveBuilder(@Nullable String name, Type type, int pp, int accuracy) {
		MoveBuilder() {  this(null); }
		// MoveBuilder(int accuracy) {  this(null, accuracy); }
		// MoveBuilder(@Nullable String name) { this(name, 0); }
		MoveBuilder(@Nullable String name) {
			this.name = name;
			// doesCharge = false;
			doesRecharge = false;
			secondaryChance = -1;
		}
		
		Move create(Moves moveEnum) {
			String name = this.name == null ? moveEnum.name().replaceAll("_", " ").trim() : this.name;
			return new Move(name, moveEnum.ordinal()+1,
				moveEnum.description, moveEnum.type,
				moveEnum.pp, moveEnum.priority,
				doesCharge, doesRecharge,
				moveEnum.accuracy, accuracyProp,
				damageEffect == null ? moveEnum.classicDamage : damageEffect.create(),
				moveCondition, onMoveMiss,
				primary == null ? null : primary.create(),
				secondary == null ? null : secondary.create(),
				secondaryChance < 0 ? moveEnum.secondaryChance : secondaryChance
			);
		}
		
		MoveBuilder accuracy(AccuracyProperty prop) {
			this.accuracyProp = prop;
			return this;
		}
		
		MoveBuilder damage(DamageBuilder damageEffect) {
			this.damageEffect = damageEffect;
			return this;
		}
		
		MoveBuilder condition(Function<MoveContext, Boolean> condition) {
			this.moveCondition = condition;
			return this;
		}
		
		MoveBuilder onMiss(Consumer<MoveContext> onMiss) {
			this.onMoveMiss = onMiss;
			return this;
		}
		
		MoveBuilder charge(ChargeState chargeState) {
			doesCharge = chargeState;
			return this;
		}
		
		MoveBuilder recharge() {
			doesRecharge = true;
			return this;
		}
		
		EffectGroupBuilder primary() {
			return new EffectGroupBuilder(this, true);
		}
		EffectGroupBuilder secondary(int chance) {
			secondaryChance = chance;
			return new EffectGroupBuilder(this, false);
		}
		
		MoveBuilder primary(EffectGroupBuilder effects) {
			this.primary = effects;
			return this;
		}
		MoveBuilder secondary(EffectGroupBuilder effects) {
			this.secondary = effects;
			return this;
		}
		
		MoveBuilder effectChance(int chance) {
			secondaryChance = chance;
			return this;
		}
		
		// add properties for classic one-hit-KO
		MoveBuilder ohko(DamageCategory damageType) {
			return accuracy(context -> context.userPokemon.getLevel() - context.enemyPokemon.getLevel() + 30)
				.condition(context -> context.userPokemon.getLevel() >= context.enemyPokemon.getLevel())
				.damage(new DamageBuilder(damageType, new PercentageDamage(100, false)))
				;
		}
	}
}
