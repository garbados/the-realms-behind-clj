# Characters

The figures that players will enact during play are mortals who have come into contact with the Fae Queen, an extraordinarily powerful extradimensional entity, and are now linked to Her and the Realms Behind to which She is connected. Players make choices on their behalf, etc.; if you're reading this, you've done this before.

Characters are built from an amount of experience (aka "xp"), which characters accumulate through their adventures. Characters begin with 75xp, which players spend buying the pieces of their avatar.

Characters have several innate components, as well as gear and learned knowledge. These are divided into attributes, skills, feats, and equipment. From those are derived matters like what spells one knows, or what abilities one has.

The costs of a character's attributes, skills, and feats scale geometrically. When you buy the first level, you pay the cost once. When you buy the second level, you pay the cost twice, for a total of three times. Once you buy the third level, you'll have paid the cost six times.

## Table of Contents

- [Attributes](#attributes)
- [Skills](#skills)
- [Feats](#feats)
- [Equipment](#equipment)
- [Stats](#stats)
- [Fungibles](#fungibles)

## Attributes

Quick facts
- Body, Mind, Spirit, Luck
- Cost: 2 / level (1:2, 2:4, 3:6, etc.)

Attributes are a numeric evaluation of a character's major inherent qualities within a few categories. These are:

- Body: Physical excellence. *Associated with Hearts.*
- Mind: Intellect and reasoning. *Associated with Spades.*
- Spirit: Grit and drive. *Associated with Clubs.*
- Luck: Favor of the universe. *Associated with Diamonds.*

Each stat has several skills associated with it, which will be covered more under [Skills](#skills). Stat and skill levels are added together during checks, so one stat point affects all the skills associated with it.

Each stat is associated with one of the four suits. Generally, including cards of this suit in checks made with a skill associated with that stat, will cause good things to happen.

## Skills

Quick facts:
- Each skill has an associated attribute.
- Cost: 1 / level (1:1, 2:2, 3:3, etc.)
- Body
    - [Acrobatics](#acrobatics): Balance, Tumble, etc.
    - [Athletics](#athletics): Climb, Swim, etc.
    - [Melee](#melee): Aptitude in close combat.
    - [Might](#might): Carrying capacity.
    - [Ranged](#ranged): Aptitude with ranged weapons.
    - [Resilience](#resilience): Health and constitution.
- Mind
    - [Lore](#lore) (subject): Specialized knowledge in a particular field.
    - [Craft](#craft): Making things.
    - [Deception](#deception): Lying.
    - [Diplomacy](#diplomacy): Making nice.
    - [Insight](#insight): Putting the clues together.
    - [Medicine](#medicine): Affects mundane and magical healing.
    - [Streetwise](#streetwise): Knowledge of and experience with cities.
    - [Survival](#survival): Knowledge of and experience with wilderness.
- Spirit
    - [Awareness](#awareness): Alert senses.
    - [Performance](#performance): Doot doot doot doot.
    - [Presence](#presence): Influence via bearing, stature, conviction.
    - [Resolve](#resolve): Will to power.
    - [Sorcery](#sorcery): Madness to focus ethereal winds.
    - [Stealth](#stealth): Sneaking, hiding, and generally avoiding notice.
- Luck
    - [Theurgy](#theurgy): Madness to call down divinity.

Quoting *FlagonQuest*:

> Skills are specific knowledges and arts that one practices and trains; swordplay, schools of magic, and diplomacy are just some of the Skills that characters can develop to best the challenges they face.

> Your [Attributes] increase some of your capabilities on their own. Your Skills are almost always used in the form of your Skill Total, which is the sum of points in that Skill and its corresponding [Attribute].

### Acrobatics
### Athletics
### Awareness
### Craft
### Deception
### Diplomacy
### Insight
### Lore
### Medicine
### Melee
### Might
### Performance
### Presence
### Ranged
### Resilience
### Resolve
### Sorcery
### Stealth
### Streetwise
### Survival
### Theurgy

## Feats

Quick facts:
- Cost: 3 / level (3:3, 2:6, 3:9, etc.)

Feats encompass talents, especial qualities, learned techniques, spells, and other notable expressions of one's particular prowess.

Feats follow a schema such that they are ultimately expressed as data files. So, only the schema is reproduced here:

- Name: The human-readable name.
- Description: A human-readable description of the feat.
- Level: The level of the feat
- When Gained: What happens or must be decided when the feat is bought.
- Effect: What the feat does!
- Requirements: What the character must satisfy to buy the feat, usually in the form of skill requirements.
- Tags: a list of unique terms associated with this feat, which indicate how it is to be understood. As examples:
    - Talent: The feat has a passive effect.
    - Technique: The feat has an associated action.
    - Repeatable: The feat may be taken multiple times.
    - Buildable: The feat must be customized before purchase.
    - Background: The feat relates to ancestry, physiology, or upbringing, and as such is generally only eligible to take during character creation.

## Equipment

Quick facts:
- Buy with gold!

Equipment includes all your gear and inventory, weapons, armor, items, etc. As materials in an economy of prices, they have associated costs in gold pieces.

Like Feats, equipment types follow a schema that allows their base types to be expressed as data files. So, again, only their schemas are reproduced here:

- Weapons:
    - Name: A human-readable name.
    - Description: A human-readable description of the weapon.
    - Level: Quality of the object.
    - Damage: How much damage is applied on a success, before other factors.
        - *Damage Attribute is always either Body or other via Feat. - dfb*
    - Defense: The parry bonus of the weapon.
    - Range: Maximum range in meters, or "close" for melee range.
    - Skill: Skill used in attacks with this weapon.
    - Might: How much Might is required to use the weapon.
    - Enchantments: Magical effects, including curses.
    - Tags: Unique terms associated with the weapon.
- Armor:
    - Name: A human-readable name.
    - Description: A human-readable description of the weapon.
    - Level: Quality of the object.
    - Resists: Damage reductions applied when wearing the armor, by type.
    - Inertia: A penalty to Dodge and Speed applied while wearing the armor.
    - Might: How much Might is required to wear the armor.
    - Enchantments: Magical effects, including curses.
    - Tags: Unique terms associated with the weapon.
- Pack:
    - Name: A human-readable name.
    - Description: A human-readable description of the weapon.
    - Level: Quality of the object.
    - Might: How much Might is required to carry this pack.
    - Encumbrance: Additional carrying capacity provided.
    - Enchantments: Magical effects, including curses.
    - Tags: Unique terms associated with the weapon.
- Items:
    - Name: A human-readable name.
    - Description: A human-readable description of the weapon.
    - Level: Quality of the object.
    - Tags: Unique terms associated with the weapon.
        - Tool: A thing used repeatedly in some activity, like a hammer.
        - Consumable: A thing destroyed when used.

## Stats

Quick facts:
- Health: Resilience ~ 1x Shallow, 2x Deep (BODY)
- Will: 2 x Resolve (SPIRIT)
- Fortune: 2 x Luck (LUCK)
- Draw: 3 x Insight (MIND)
- Speed: 2 x Athletics (BODY)
- Initiative: 2 x Awareness (SPIRIT)
- Defenses: 8 + {Skill}
    - Parry: Melee
    - Dodge: Acrobatics
    - Body: Resilience
    - Mind: Insight
    - Spirit: Resolve
    - Luck: Theurgy

## Fungibles

Quick facts:
- Health: Characters have *shallow health* and *deep health*. Damage destroys shallow health first unless otherwise stated. When you have no shallow health, you are Wounded: make all checks at disadvantage, and lower all Defenses by 2. When you have no deep health, you are Downed: you fall unconscious, and may die. Any healing to shallow health while Downed instead restores one deep health. (If the whole party is Downed, their fate falls to the GM.)
- Hand: When the day begins, draw {Draw} cards; this is your hand. When you run out of cards in hand, you're Exhausted: you can only play cards from the top of your deck, and your Defenses are lowered by 2. If you run out of a deck, you're Stupefied: you automatically fail any checks you are forced to make, count as Downed, and all your Defenses are lowered by 8.
- Will: Spend a point to discard up to {Insight} cards, and then draw that many plus one.
- Fortune: Spend a point to gain advantage on a check, or to counter disadvantage.
- Madness: A form of unbalanced mystic residue which progressively causes insanity as well as temporal and spatial flux. At any time, you may spend one Will to remove one Madness. When resolving a check, first sift cards equal to your current Madness, adding them to the suit pool. This is primarily caused by using magic, but can also be afflicted. If you have more Madness than Health, you become Stupefied.
