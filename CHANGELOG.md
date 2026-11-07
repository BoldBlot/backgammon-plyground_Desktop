# Backgammon PlyGround for desktop: changelog

The bracket is the Android version this one was last brought up to date with.

## 0.2.1 (A0.8.8) — not released yet

Brought up to date with Android 0.8.8, and the first round of comments on 0.2.0.

From Android 0.8.7 and 0.8.8:

- **New PureTD networks:** new wide 5L networks and a compact 4L pair, chosen in Preferences, Advanced engine settings. An analysis with the compact pair is recorded as "PureTD 4L".
- **Cubeful checker play in money games**, on by default, with a toggle beside the network choice. Matches are unchanged.
- **Faster background analysis:** a cube decision that 2-ply settles by a wide margin isn't searched deeper. Analyze again always uses the full depth.
- **Match Library:** a progress bar and "Analyzing: n of m decisions" under a match being analyzed.
- **Clicks on the board:** the bear-off tray bears off the most checkers; an empty point or an opponent's blot makes that point, if the dice allow it.
- **Me first in .mat files** (Preferences, Data and help): on by default, you are Player 1 in exports and the first player on import. Turn it off for the old order.

From the comments:

- **Ctrl+V pastes an XGID** or GNU ID from anywhere on the main window into Analyze Position, which also has a Paste XGID button. In a text box, Ctrl+V still pastes into the box. Something that isn't a position leaves you where you are, with a message.
- **Review:** the button beside Checker and Cube is now **Evaluate**, replacing the one that opened Analyze Position. It asks for the engine and depths, then evaluates that one move or cube decision again.
- **Menus:** unavailable items are a lighter grey.
- **My positions:** the cube in a thumbnail is half the size, with its number at 8 sp, the one exception to the 14 sp minimum.

## 0.2.0 (A0.8.6) — 2026-11-07 (packages uploaded, not linked from the website)

A desktop application's layout, in place of the phone's screens.

- **It opens on the board**, with the new game options beside it. An unfinished match shows on the board, with Continue match first.
- **One main window:** menu bar, toolbar, board, side panel and status bar. Playing, reviewing, analyzing a position and the quiz all happen there, on the same board.
- **Seven menus** reach everything: File, Edit, View, Game, Analyze, Study, Help. New shortcuts: Ctrl+N, Ctrl+S, Ctrl+E, Ctrl+Z, Ctrl+comma, Ctrl+1, 2 and 3, F1, F9, H, M.
- **Separate windows:** Match Library (a sortable table), My positions (a tree of folders beside the positions) and Statistics stay open beside the board. Click selects, Ctrl and Shift select several, a double click opens on the main board, the right button gives a menu in the library and the folder tree. Ctrl+W closes them.
- **Dialogs:** Preferences (all settings groups in one dialog), Quiz setup, Speed test, About and Welcome.
- **Review:** the move list and the analysis show together in the side panel; Played/Best and the view chips sit under the board.
- **Analyze Position and saved positions:** tabs in the side panel (Setup or Details, and Analysis).
- **Status bar:** short messages, analysis progress with Stop, and Autofinish.
- The side panel is on the right by default; View can hide it or move it.
- **Text:** nothing smaller than 14 sp, the board's point numbers, the chart labels and the menu bar included.
- **Back button:** a view opened over another one (a review over the game, say) has a button back to it at the top of the side panel, on the toolbar and in the Game menu.
- **Board and panel:** dice 20% smaller, more room between chips, your score at the bottom of the side panel, Quick quiz on the toolbar.
- **Review:** an Analyze Position button beside Checker and Cube opens that position in Analyze Position, sides as on the board, with a button back to the review. The computer's own move is tagged "computer" in the list, not "you".
- **Switches** sit right before their text, like radio buttons, instead of at the far side of the window.
- **The computer's decisions are analyzed by default**, at the same depths as the player's.
- **About:** points to this project's own source code, and says that one Open Sage file is changed for the desktop build.
- Gone: the Home screen, Back arrows, ⋮ menus, the portrait layout for tall windows, touch-sized minimum targets.
- **Fixed:** the quiz could crash on opening, when its first question was asked before its list of seen positions existed.

## 0.1.0 (A0.8.6) — 2026-11-07 (git tag v0.1.0, not released)

The straight copy of the Android app (commit 80fe819, 0.8.6, build 69), playing, reviewing and analyzing in a window on Linux.
