# AutoMaster
## D&D Dungeon Master Mode
NOTE: This list of commands needs updating
Tool for automated role playing game master functions

1) advturn | advanceturn => advances play to the next entity
2) amon | addmonster <monster index or name> <personal name> => adds monster
3) apc | addplayercharacter <personal name> <initiative> => add character
4) at | attack <attack index> => makes attack by current monster
5) cast <spellname> <level - optional, defaults to lowers possible level> => cast spell but current monster
6) cur | currententity => gives name of current in initiative
7) curhp => gives hp of current entity in initiative
8) heal <index of entity> <hp> => heals monster
9) hit <index of entity> <hp> => removes monster hp
10) gr | getround => gives round number
11) la | listattack => prints list of current monster attacks
12) lam | listallmonsters => prints list of monster templates available for play
13) li | listinitiative => gives print of characters/monsters in initiative order
14) lms | load_monster_set <filename> => loads set of monsters into initiative order
15) ls | listspells => prints list of current monster spells
16) lss | listspellslots => prints list of current monster spell slots
17) rm | remove <index or personal name> => removes entity from initiative order
18) sc | startcombat => starts play
19) act <action name> => take action
20) listact => lists actions

Actions
Uses the same engine as spells for spell-like actions. 

Spells
Support spells, which are not targeted. DC rolls support.
Supports automatic concentration checks

## DND Single Player Mode
TODO - Need to write this section

##Warhammer 40k
This mode allows users to create armies in 40k_config/armies. Several example files are provided. To run the program, execute exec_40k.bat.

Enter "help" to the command prompt to see the list of commands.