import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

public class Game extends JPanel {
    public static final String CARD_GAME = "game";
    public static final String CARD_MAIN = "main";
    private GameTypePanel mainForm;
    private GamePanel gamePanel;

    public Game() {
        this.init();
    }

    protected void init() {
        this.setLayout(new CardLayout());

        gamePanel = new GamePanel(this);
        this.add(gamePanel, CARD_GAME);

        mainForm = new GameTypePanel() {
            @Override
            protected void onGameStart(PlayerType typeA, PlayerType typeB) {
                showGameBoard();
                gamePanel.startGame(typeA, typeB);
            }
        };
        this.add(mainForm, CARD_MAIN);

        this.showMenu();
    }

    protected void showMenu() {
        CardLayout cl = (CardLayout) (this.getLayout());
        cl.show(this, CARD_MAIN);
    }

    protected void showGameBoard() {
        CardLayout cl = (CardLayout) (this.getLayout());
        cl.show(this, CARD_GAME);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tic Tac Toe");

        Game game = new Game();
        frame.setContentPane(game);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    
    public class Board {

        protected static final int BOARD_SIZE = 3;

        private Cell[] cells = new Cell[BOARD_SIZE * BOARD_SIZE];

        private CellChangeListener cellChangeListener;
        private GameStateChangeListener gameStateChangeListener;

        private Player playerA;
        private Player playerB;
        private Player currentPlayer;

        private GameState gameState = GameState.NOT_STARTED;

        public Board() {
            this.clear();
        }

        protected void clear() {
            for (int i = 0; i < cells.length; i++) {
                cells[i] = Cell.EMPTY;
            }
        }

        public void onClick(int index) {
            if (gameState != GameState.PLAYER_MOVE) {
                return;
            }

            Cell identity = currentPlayer.getIdentity();

            cells[index] = identity;

            if (cellChangeListener != null) {
                cellChangeListener.onCellStateChange(index, identity, false);
            }

            boolean stateChanged = this.checkGameState();

            if (gameState == GameState.PLAYER_MOVE) {
                this.nextMove();
            }

            if (stateChanged && gameStateChangeListener != null) {
                gameStateChangeListener.onGameStateChange(gameState);
            }
        }

        public void setGameStateChangeListener(GameStateChangeListener gameStateChangeListener) {
            this.gameStateChangeListener = gameStateChangeListener;
        }

        public void setCellChangeListener(CellChangeListener listener) {
            this.cellChangeListener = listener;
        }

        public void startGame(PlayerType typeA, PlayerType typeB) {
            if (typeA == PlayerType.HUMAN) {
                playerA = new HumanPlayer("Player 1", Cell.X);
            } else {
                playerA = new ComputerPlayer("Computer 1", Cell.X);
            }

            if (typeB == PlayerType.HUMAN) {
                playerB = new HumanPlayer("Player 2", Cell.O);
            } else {
                playerB = new ComputerPlayer("Computer 2", Cell.O);
            }

            currentPlayer = playerA;

            gameState = GameState.PLAYER_MOVE;
            this.clear();

            if (!currentPlayer.isHuman()) {
                currentPlayer.nextMove(this);
            }
        }

        public boolean checkGameState() {
            if (gameState != GameState.PLAYER_MOVE) {
                return false;
            }

            Cell identity = currentPlayer.getIdentity();
            if (this.isWinRow(identity)) {
                gameState = GameState.PLAYER_WIN;
                return true;
            }
            if (this.isWinColumn(identity)) {
                gameState = GameState.PLAYER_WIN;
                return true;
            }
            if (this.isWinCross(identity)) {
                gameState = GameState.PLAYER_WIN;
                return true;
            }
            if (this.isWinReverseCross(identity)) {
                gameState = GameState.PLAYER_WIN;
                return true;
            }

            if (!this.isEmptyCellExist()) {
                gameState = GameState.DRAW;
                return true;
            }

            return false;
        }

        private boolean isWinRow(Cell identity) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    Cell cell = cells[row + col * BOARD_SIZE];
                    if (cell != identity) {
                        break;
                    }
                    if (col == BOARD_SIZE - 1) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isWinColumn(Cell identity) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                for (int row = 0; row < BOARD_SIZE; row++) {
                    Cell cell = cells[row + col * BOARD_SIZE];
                    if (cell != identity) {
                        break;
                    }
                    if (row == BOARD_SIZE - 1) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isWinCross(Cell identity) {
            for (int idx = 0; idx < BOARD_SIZE * BOARD_SIZE; idx += BOARD_SIZE + 1) {
                Cell cell = cells[idx];
                if (cell != identity) {
                    return false;
                }
            }
            return true;
        }

        private boolean isWinReverseCross(Cell identity) {
            for (int idx = BOARD_SIZE - 1; idx < BOARD_SIZE * BOARD_SIZE - 1; idx += BOARD_SIZE - 1) {
                Cell cell = cells[idx];
                if (cell != identity) {
                    return false;
                }
            }

            return true;
        }

        private boolean isEmptyCellExist() {
            for (Cell cell : cells) {
                if (cell.isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        public void nextMove() {
            if (currentPlayer == playerA) {
                currentPlayer = playerB;
            } else {
                currentPlayer = playerA;
            }

            currentPlayer.nextMove(this);
        }

        public boolean isValidMove(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= BOARD_SIZE) {
                throw new IllegalArgumentException("Row has invalid value: " + rowIndex);
            }
            if (columnIndex < 0 || columnIndex >= BOARD_SIZE) {
                throw new IllegalArgumentException("Column has invalid value: " + rowIndex);
            }
            Cell cell = cells[rowIndex + columnIndex * BOARD_SIZE];

            return cell == Cell.EMPTY;
        }

        public Player getCurrentPlayer() {
            return currentPlayer;
        }
    }

    
    public enum Cell {
        EMPTY(""),
        X("X"),
        O("O");

        private final String text;

        private Cell(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public boolean isEmpty(){
            return this == EMPTY;
        }
    }

    
    public interface CellChangeListener {
        void onCellStateChange(int index, Cell cell, boolean enabled);
    }

   
    public class ComputerPlayer extends Player {

        public ComputerPlayer(String name, Cell identity) {
            super(name, identity);
        }

        @Override
        public boolean isHuman() {
            return false;
        }

        @Override
        public void nextMove(Board board) {
            int move = this.generateRandomMove(board);
            board.onClick(move);
        }

        public int generateRandomMove(Board board) {
            Random rand = new Random();

            int rowIndex;
            int columnIndex;
            do {
                rowIndex = rand.nextInt(Board.BOARD_SIZE);
                columnIndex = rand.nextInt(Board.BOARD_SIZE);
            }
            while (!board.isValidMove(rowIndex, columnIndex));

            return rowIndex + columnIndex * Board.BOARD_SIZE;
        }
    }

   
    public class GamePanel extends JPanel implements GameStateChangeListener, CellChangeListener {

        private Game game;
        private JButton[] buttons = new JButton[Board.BOARD_SIZE * Board.BOARD_SIZE];
        private Board board = new Board();

        public GamePanel(Game game) {
            this.game = game;
            createControls();
            board.setGameStateChangeListener(this);
            board.setCellChangeListener(this);
        }

        private void createControls() {
            this.setLayout(new GridLayout(Board.BOARD_SIZE, Board.BOARD_SIZE, 4, 4));

            for (int i = 0; i < buttons.length; i++) {
                final int index = i;
                JButton button = new JButton();

                Font font = button.getFont();
                button.setFont(font.deriveFont(1, 36f));
                button.setFocusPainted(false);
                button.addActionListener(new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        board.onClick(index);
                    }
                });
                buttons[i] = button;
                this.add(button);
            }

            this.clear();
        }

        protected void clear() {
            for (JButton button : buttons) {
                button.setEnabled(false);
                button.setText(Cell.EMPTY.getText());
            }
        }

        protected void startGame() {
            for (JButton button : buttons) {
                button.setEnabled(true);
                button.setText(Cell.EMPTY.getText());
            }
        }

        public void onGameStateChange(GameState state) {
            this.setEnabled(false);
            if (state == GameState.PLAYER_WIN) {
                JOptionPane.showMessageDialog(this, board.getCurrentPlayer().getName() + " Win!");
            } else if (state == GameState.DRAW) {
                JOptionPane.showMessageDialog(this, "Draw!");
            }
            game.showMenu();
        }

        public void onCellStateChange(int index, Cell cell, boolean enabled) {
            buttons[index].setEnabled(enabled);
            buttons[index].setText(cell.getText());
        }

        public void startGame(PlayerType typeA, PlayerType typeB) {
            this.startGame();
            board.startGame(typeA, typeB);
        }
    }

   
    public enum GameState {
        NOT_STARTED,
        PLAYER_MOVE,
        PLAYER_WIN,
        DRAW
    }

    
    public interface GameStateChangeListener {
        void onGameStateChange(GameState state);
    }

    
    public abstract class GameTypePanel extends JPanel {
        public GameTypePanel() {
            this.creteComponents();
        }

        private void creteComponents() {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(Box.createRigidArea(new Dimension(0, 50)));
            this.add(this.getGameTypeButton("Player vs Player", PlayerType.HUMAN, PlayerType.HUMAN));
            this.add(Box.createRigidArea(new Dimension(0, 15)));
            this.add(this.getGameTypeButton("Player vs Computer", PlayerType.HUMAN, PlayerType.COMPUTER));
            this.add(Box.createRigidArea(new Dimension(0, 15)));
            this.add(this.getGameTypeButton("Computer vs Computer", PlayerType.COMPUTER, PlayerType.COMPUTER));
        }

        private JButton getGameTypeButton(String text, final PlayerType typeA, final PlayerType typeB) {
            JButton button = new JButton(text);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onGameStart(typeA, typeB);
                }
            });
            return button;
        }

        protected abstract void onGameStart(PlayerType typeA, PlayerType typeB);
    }

   
    public class HumanPlayer extends Player {
        public HumanPlayer(String name, Cell identity) {
            super(name, identity);
        }

        @Override
        public boolean isHuman() {
            return true;
        }

        @Override
        public void nextMove(Board board) {

        }
    }

    enum PlayerType {
        HUMAN, COMPUTER;
    }

   
    public abstract class Player {

        private String name;

        private Cell identity;

        public Player(String name, Cell identity) {
            this.name = name;
            this.identity = identity;
        }

        public String getName() {
            return name;
        }

        public Cell getIdentity() {
            return identity;
        }

        public abstract void nextMove(Board board);

        public abstract boolean isHuman();
    }
}

