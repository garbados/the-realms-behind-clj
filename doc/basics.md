# Basics

## Checks

*The Realms Behind* is a *d13* game, meaning it uses 52-card decks as its source of randomness, rather than a *d20*, for example. However, the deck you use will likely change as your character grows. (The GM may also augment starting decks with Jokers, if it pleases them.)

At the beginning of each day, players draw cards from their deck. They spend these cards during *checks* by revealing them. Once the check is resolved, the cards are discarded. *The player does not draw replacement cards.*

Checks pit the rank of the highest revealed card, plus any modifiers, against a threshold, such as a creature's defenses or a lock's complexity. Modifiers include the relevant skill, circumstantial bonuses, suit bonuses, and so on. If the rank plus the modifiers meets or exceeds the threshold, the check succeeds.

Some checks may demand a response in a certain suit. Ex: A poison check may demand a response in Hearts. An enchantment spell may demand a response in Spades. If you do not have that suit in hand, you must reveal the top card of your deck until you find an applicable suit or you become Stupefied. None of these discarded cards apply to the suit pool. You may spend a Fortune to respond in any suit.

### Advantage and Disadvantage

Characters sometimes enjoy advantages on their checks, or suffer disadvantages. They cancel each other out, but can stack themselves.

- Advantage: You may play another card from your hand OR play a card from the top of your deck. Its suit is added to the Suit Pool, and you may choose to use its rank instead of another card.
- Disadvantage: Play another card from the top of your deck. You must use its rank if it is lowest among all cards played. It does not count as part of the Suit Pool.

Advantage and disadvantage cancel each other out. One advantage plus two disadvantages equals one disadvantage.

## Assisting on Checks

If a character is making a check, other characters may *assist* with some relevant skill. Rather than flipping a card from hand, the assisting player flips a card from the top of their deck against DC 11, using the relevant skill. If this check succeeds, the character being assisted gains +1 Advantage and an extra success if their check success.

### Fungibles

Players manage a small number of resources:

- Health: Your character's health, shallow and deep.
- Hand: Your cards in hand.
- Will: Spend a point to discard several cards, drawing that many plus one.
- Fortune: Spend a point to gain advantage, to counter disadvantage, etc.
- Madness: A form of unbalanced mystic residue which progressively causes insanity as well as temporal and spatial flux.

See [Characters > Fungibles](./characters.md#fungibles) for more information.

### Damage

Damage is calculated when an attack check succeeds. It is equal to the base damage of the attack, plus any Hearts in the Suit Pool, minus any relevant Resists on the target. The remainder is taken from Health.

### Days and Resting

- "A Day": All time between long rests is considered "a day". If you're forced to stay awake for 72 hours, that's a day.
- Short Rest: The party may take up to two short rests during a day. These each represent about an hour of downtime, to patch up, meditate, etc.
    - Restore {Resolve} Will and {Theurgy} Fortune
    - Remove {Resilience} Madness.
    - Restore {Medicine} plus one Shallow Health.
    - Draw {Insight} cards.
- Long Rest: The party concludes their day with 8-10 hours of sleep and preparations.
    - Combine your hand, discards, and deck. Shuffle them.
    - Heal {Body + Medicine} Shallow or Deep Health.
    - Restore Will and Fortune to their maximums.
    - Reset Madness to zero.
    - Draw {Draw} cards.

## Elements

Resists come in these types:

- Physical
- Fire
- Frost
- Brilliant
- Shadow

... and as Weaknesses!

### Actions and Time

Actions encapsulate player activity, and represent the flow of time as well. There are several *phases* of time, during which actions represent more or less time:

- Encounter: An action is about 3 seconds. A turn is about ten.
- Exploration: An action is between one and three hours. There are about five exploration-phase actions of activity during a day.
- Downtime: About a day's work. Includes wage labor, crafting, long-term research, etc.

### Status Effects

Players may encounter various status effects. They are roughly grouped by associated attribute:

- Body, Hearts:
    - Bleeding: At the beginning of your turn, take one damage (ignoring resists), and remove one stack of Bleeding.
    - Crippled: Suffer -1 to Attack actions per stack. Remove one stack at the beginning of your turn.
    - Harried: Suffer -1 to Parry and Dodge per stack. Remove one stack at the beginning of your turn.
    - Sickened: Suffer -1 to Body and Luck defenses per stack. Remove one stack at the beginning of your turn.
    - Necrotic: If you would heal, first remove that many stacks of Necrosis, and only heal any remainder. Remove one stack per Long Rest.
- Mind, Spades:
    - Berserk: You're on a rampage, and will attack anyone in sight! You make attacks with +1 Advantage and +1 success, when they succeed. Remove one stack at the beginning of your turn.
    - Enchanted: You cannot harm anyone willingly, and will peacefully remove yourself from combat. Remove one stack at the beginning of your turn.
    - Fogged: Suffer -1 to Mind and Spirit defenses per stack. Remove one stack at the beginning of your turn.
    - Taunted: Suffer -1 to checks against any target but they that taunted you, per stack. Remove one stack at the beginning of your turn. Remove all stacks when that target is downed.
    - Exposed / Warded: Suffer -1 / gain +1 Fire, Frost, Shadow, and Brilliant resist, per stack. Remove one stack at the beginning of your turn. 
    - Dispelled: Suffer -1 to Sorcery and Theurgy checks per stack. Remove one stack at the beginning of your turn.
- Spirit, Clubs:
    - Burning: Take two damage (before resists) at the beginning of your turn, and remove one stack. Lowering your temperature can remove stacks, such as by dunking yourself in water.
    - Frozen: As *Vulnerable* and *Slowed*.
    - Frightened: Suffer Bad Luck on all checks. For each check made, remove a stack of Frightened. Remove a stack at the beginning of your turn. *Characters that are frightened enough may just run away!*
    - Slowed / Hasted : Suffer -1 / gain +1 to Speed per stack. Remove one stack at the beginning of your turn.
- Luck, Diamonds:
    - Vulnerable: If you would take damage, after resists, take one additional damage per stack. At the beginning of your turn, remove one stack.
    - Protected: If you would take damage, after resists, first remove that many stacks of Protected, up to as many stacks as the character has. For each stack removed, suffer one less damage. At the beginning of your turn, remove one stack.
