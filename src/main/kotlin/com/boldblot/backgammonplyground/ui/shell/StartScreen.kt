package com.boldblot.backgammonplyground.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.boldblot.backgammonplyground.PlyGroundApp
import com.boldblot.backgammonplyground.core.Board
import com.boldblot.backgammonplyground.core.CpuLevel
import com.boldblot.backgammonplyground.core.CubeState
import com.boldblot.backgammonplyground.core.MatchRules
import com.boldblot.backgammonplyground.core.MistakeRate
import com.boldblot.backgammonplyground.core.Player
import com.boldblot.backgammonplyground.core.EngineKind
import com.boldblot.backgammonplyground.data.Settings
import com.boldblot.backgammonplyground.ui.board.BoardView
import com.boldblot.backgammonplyground.ui.board.boardColorsFor
import com.boldblot.backgammonplyground.ui.game.GameViewModel
import com.boldblot.backgammonplyground.ui.home.NewGamePanel

/**
 * What the app opens on: the board, with the new game options beside it. The board shows the starting position, or the
 * unfinished match, which Continue match goes back to.
 */
@Composable
fun StartScreen(
    app: PlyGroundApp, gameVm: GameViewModel, hasMatch: Boolean, onContinue: () -> Unit,
    onStart: (MatchRules, CpuLevel, MistakeRate?, EngineKind) -> Unit,
) {
    val s by app.settings.settings.collectAsState(initial = Settings())
    val game by gameVm.state.collectAsState()
    val g = game.match?.takeIf { hasMatch }?.game
    val board = g?.board ?: Board.INITIAL
    BoardAndPanel(
        panelRight = s.railRight,
        board = { m ->
            BoardView(
                board = board, modifier = m, colors = boardColorsFor(s), mirrored = s.mirrorBoard,
                cube = g?.cube ?: CubeState(), crawford = g?.crawford ?: false, crawfordSign = s.crawfordSign,
                pips = if (s.showPips) listOf(board.pipCount(Player.HUMAN), board.pipCount(Player.CPU)) else null,
            )
        },
    ) {
        NewGamePanel(app, hasMatch, onContinue, onStart)
    }
}
