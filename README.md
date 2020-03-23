# AutoMaster
Tool for automated role playing game master functions

advturn | advanceturn => advances play to the next entity
amon | addmonster <monster index or name> <personal name> => adds monster
apc | addplayercharacter <personal name> <initiative> => add character
at | attack <attack index> => makes attack by current monster
cast <spellname> <level - optional, defaults to lowers possible level> => cast spell but current monster
cur | currententity => gives name of current in initiative
curhp => gives hp of current entity in initiative
heal <index of entity> <hp> => heals monster
hit <index of entity> <hp> => removes monster hp
gr | getround => gives round number
la | listattack => prints list of current monster attacks
lam | listallmonsters => prints list of monster templates available for play
li | listinitiative => gives print of characters/monsters in initiative order
lms | load_monster_set <filename> => loads set of monsters into initiative order
ls | listspells => prints list of current monster spells
lss | listspellslots => prints list of current monster spell slots
rm | remove <index or personal name> => removes entity from initiative order
sc | startcombat => starts play
act <action name> => take action
listact => lists actions

Actions
Uses the same engine as spells for spell-like actions. 

Spells
Support spells, which are not targeted. DC rolls support.
Supports automatic concentration checks

