package Table;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.*;
import javax.swing.SpringLayout.Constraints;

import java.util.HashMap;
import java.util.Map;

public class Sheet extends JPanel implements MouseListener, MouseWheelListener {

    private static int border = 3; /// size of border between adjacent cells

    /// the HashMap containing all of the cells
    private Map<CellIdentifier, Cell> cells = new HashMap<CellIdentifier, Cell>();

    private Cell focusedCell; /// the cell currently being focused
    private Cell topLeftCell; /// the cell in the top left corner of the sheet
    private Cell bottomRightCell; /// the fully visible cell in the bottom right corner of the sheet

    public Sheet() {
        super();
        createCell(0, 0);
        createCell(1, 0);
        createCell(0, 1);
        focusedCell = createCell(1, 1);
        topLeftCell = focusedCell;
        bottomRightCell = topLeftCell;
        focusedCell.setBackground(Color.GRAY);
        setBackground(Color.black);
        setLayout(new SpringLayout());
        setOpaque(true);
        setFocusable(true);
    }

    public void init() {
        makeSheet();
        changeFocus(focusedCell);
    }

    private Cell createCell(int column, int row) {
        CellIdentifier id = new CellIdentifier(CellIdentifier.columnNumberToString(column), row);
        Cell cell = new Cell(id);
        cells.put(id, cell);
        cell.addMouseListener(this);
        cell.addMouseWheelListener(this);
        return cell;
    }

    private void makeSheet() { /// tunes the SpringLayout of the Sheet
        removeAll(); /// clears the grid from all of its components, they will be added again
        makeCompactGrid();
        revalidate();
    }

    private void changeFocus(Cell cell) {
        focusedCell.setBackground(Color.WHITE);
        focusedCell = cell;
        focusedCell.setBackground(Color.GRAY);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Object obj = e.getSource();
        if (obj instanceof Cell)
            changeFocus((Cell) obj);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) { /// moved up
            CellIdentifier topLeftId = topLeftCell.getCellIdentifier();
            int row = topLeftId.getRow();
            if (row > 1) {
                row--;
                CellIdentifier newTopLeftId = new CellIdentifier(topLeftId.getColumn(), row);
                topLeftCell = cells.get(newTopLeftId);
                if (topLeftCell == null)
                    topLeftCell = createCell(topLeftId.getColumnNumber(), row);
                makeSheet();
            }
        } else {
            CellIdentifier topLeftId = topLeftCell.getCellIdentifier();
            int row = topLeftId.getRow();
            row++;
            CellIdentifier newTopLeftId = new CellIdentifier(topLeftId.getColumn(), row);
            topLeftCell = cells.get(newTopLeftId);
            if (topLeftCell == null)
                createCell(topLeftId.getColumnNumber(), row);
            makeSheet();
        }
    }

    public Cell getFocusedCell() {
        return focusedCell;
    }

    public void keyPress(KeyEvent e) {
        CellIdentifier focusedCellIdentifier = focusedCell.getCellIdentifier();
        int focusedColumn = CellIdentifier.columnStringToNumber(focusedCellIdentifier.getColumn());
        int focusedRow = focusedCellIdentifier.getRow();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (e.isControlDown()) {
                    focusedRow = 1;
                } else if (focusedRow > 1) {
                    focusedRow--;
                }
                break;
            case KeyEvent.VK_DOWN:
                focusedRow++;
                break;
            case KeyEvent.VK_LEFT:
                if (e.isControlDown()) {
                    focusedColumn = 1;
                } else if (focusedColumn > 1) {
                    focusedColumn--;
                }
                break;
            case KeyEvent.VK_RIGHT:
                focusedColumn++;
                break;
        }
        e.consume();

        String column = CellIdentifier.columnNumberToString(focusedColumn);

        if (focusedColumn < topLeftCell.getCellIdentifier().getColumnNumber()) {
            topLeftCell = cells.get(new CellIdentifier(focusedColumn, topLeftCell.getCellIdentifier().getRow()));
            if (topLeftCell == null)
                topLeftCell = createCell(focusedColumn, topLeftCell.getCellIdentifier().getRow());
            makeSheet();
        }
        if (focusedRow < topLeftCell.getCellIdentifier().getRow()) {
            topLeftCell = cells.get(new CellIdentifier(topLeftCell.getCellIdentifier().getColumnNumber(), focusedRow));
            if (topLeftCell == null)
                topLeftCell = createCell(topLeftCell.getCellIdentifier().getColumnNumber(), focusedRow);
            makeSheet();
        }
        if (focusedColumn >= bottomRightCell.getCellIdentifier().getColumnNumber()) {
            topLeftCell = cells.get(new CellIdentifier(topLeftCell.getCellIdentifier().getColumnNumber() + 1,
                    topLeftCell.getCellIdentifier().getRow()));
            if (topLeftCell == null)
                topLeftCell = createCell(topLeftCell.getCellIdentifier().getColumnNumber() + 1,
                        topLeftCell.getCellIdentifier().getRow());
            makeSheet();
        }
        if (focusedRow >= bottomRightCell.getCellIdentifier().getRow()) {
            topLeftCell = cells.get(new CellIdentifier(topLeftCell.getCellIdentifier().getColumnNumber(),
                    topLeftCell.getCellIdentifier().getRow() + 1));
            if (topLeftCell == null)
                topLeftCell = createCell(topLeftCell.getCellIdentifier().getColumnNumber(),
                        topLeftCell.getCellIdentifier().getRow() + 1);
            makeSheet();
        }
        Cell newFocus = cells.get(new CellIdentifier(column, focusedRow));
        changeFocus(newFocus);
    }

    public void setText(String text) {
        focusedCell.setText(text);
        makeSheet();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    public void makeCompactGrid() {
        SpringLayout layout = (SpringLayout) this.getLayout();
        /// X and Y coordinate counters
        int x = border;
        int y = border;

        /// height and width of the container
        int height = getHeight();
        int width = getWidth();

        /// row and column of cell being looked at currently
        int column = 0;
        int row = 0;

        /// bottom right column and row
        int lastColumn = 0;
        int lastRow = 0;

        int maxWidth;
        do {
            maxWidth = 0;
            y = border;
            int i = getComponentCount();
            do {
                Cell cell = cells.get(new CellIdentifier(column, row));
                if (cell == null) {
                    cell = createCell(column, row);
                }
                if (row == 0) {
                    row = topLeftCell.getCellIdentifier().getRow();
                } else {
                    row++;
                }
                add(cell);
                Constraints constraints = layout.getConstraints(cell);
                maxWidth = Math.max(maxWidth, constraints.getWidth().getValue());
                constraints.setY(Spring.constant(y));
                constraints.setX(Spring.constant(x));
                y += constraints.getHeight().getValue();
                y += border;
            } while (y < height);
            if (column == 0) {
                column = topLeftCell.getCellIdentifier().getColumnNumber();
            } else {
                column++;
            }
            x += maxWidth;
            x += border;
            lastRow = row;
            for (; i < getComponentCount(); i++) {
                Component component = getComponent(i);
                layout.getConstraints(component).setWidth(Spring.constant(maxWidth));
            }
            row = 0;
        } while (x < width);
        lastColumn = column;
        lastColumn--;
        lastRow--;
        bottomRightCell = cells.get(new CellIdentifier(lastColumn, lastRow));
    }

    public static void main(String[] args) {
        // Frame f = new Frame();
        // f.setSize(500, 500);
        // Sheet s = new Sheet();
        // f.add(s);
        // f.setVisible(true);
        // s.init();
    }
}