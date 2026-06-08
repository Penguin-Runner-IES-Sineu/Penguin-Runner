/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package iessineu.penguinrunner;

/**
 *
 * @author loren
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class AsciiMapStudio extends JFrame {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final java.lang.reflect.Type MAP_LIST_TYPE = new TypeToken<List<MapEntry>>() {}.getType();

    private File currentFile;
    private final DefaultComboBoxModel<MapEntry> mapComboModel = new DefaultComboBoxModel<>();
    private final JComboBox<MapEntry> mapCombo = new JComboBox<>(mapComboModel);

    private final JTextField typeField = new JTextField("map", 12);
    private final JSpinner levelSpinner = new JSpinner(new SpinnerNumberModel(1, -999999, 999999, 1));
    private final JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(32, 1, 500, 1));
    private final JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(22, 1, 500, 1));
    private final JTextField paintCharField = new JTextField("#", 2);
    private final JCheckBox paintMode = new JCheckBox("Pintar amb clic dret", true);

    private final GridModel gridModel = new GridModel();
    private final JTable gridTable = new JTable(gridModel);
    private boolean loadingUi = false;

    // Inicia el programa.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AsciiMapStudio studio = new AsciiMapStudio();
            studio.setVisible(true);
            if (args.length > 0) {
                studio.openFile(new File(args[0]));
            }
        });
    }

    // Prepara la finestra principal.
    public AsciiMapStudio() {
        super("ASCII Map Studio");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setLocationByPlatform(true);
        buildMenu();
        buildUi();
        newMap();
    }

    // Crea el menú superior de fitxer.
    private void buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("Fitxer");

        JMenuItem open = new JMenuItem("Obrir JSON...");
        open.addActionListener(e -> chooseOpen());

        JMenuItem save = new JMenuItem("Guardar");
        save.addActionListener(e -> save(false));

        JMenuItem saveAs = new JMenuItem("Guardar com...");
        saveAs.addActionListener(e -> save(true));

        JMenuItem exit = new JMenuItem("Sortir");
        exit.addActionListener(e -> dispose());

        file.add(open);
        file.add(save);
        file.add(saveAs);
        file.addSeparator();
        file.add(exit);
        bar.add(file);
        setJMenuBar(bar);
    }

    // Crea tots els botons, camps i la graella.
    private void buildUi() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton openButton = new JButton("Obrir");
        JButton saveButton = new JButton("Guardar");
        JButton saveAsButton = new JButton("Guardar com");
        JButton addButton = new JButton("Nou mapa");
        JButton duplicateButton = new JButton("Duplicar");
        JButton deleteButton = new JButton("Eliminar");

        openButton.addActionListener(e -> chooseOpen());
        saveButton.addActionListener(e -> save(false));
        saveAsButton.addActionListener(e -> save(true));
        addButton.addActionListener(e -> newMap());
        duplicateButton.addActionListener(e -> duplicateMap());
        deleteButton.addActionListener(e -> deleteSelectedMap());

        mapCombo.setPrototypeDisplayValue(new MapEntry("map", 999, Collections.singletonList("###############################")));
        mapCombo.addActionListener(e -> loadSelectedMapToUi());

        top.add(new JLabel("Mapa:"));
        top.add(mapCombo);
        top.add(addButton);
        top.add(duplicateButton);
        top.add(deleteButton);
        top.add(new JSeparator(SwingConstants.VERTICAL));
        top.add(openButton);
        top.add(saveButton);
        top.add(saveAsButton);

        JPanel params = new JPanel(new FlowLayout(FlowLayout.LEFT));
        params.setBorder(BorderFactory.createTitledBorder("Paràmetres"));
        params.add(new JLabel("type:"));
        params.add(typeField);
        params.add(new JLabel("level:"));
        params.add(levelSpinner);
        params.add(new JLabel("Amplada visible:"));
        params.add(widthSpinner);
        params.add(new JLabel("Alçada visible:"));
        params.add(heightSpinner);
        params.add(new JLabel("Caràcter:"));
        paintCharField.setDocument(new OneCharDocument());
        params.add(paintCharField);
        params.add(paintMode);

        typeField.getDocument().addDocumentListener((SimpleDocumentListener) e -> updateCurrentMapFromUi());
        levelSpinner.addChangeListener(e -> updateCurrentMapFromUi());
        widthSpinner.addChangeListener(e -> resizeVisibleGrid());
        heightSpinner.addChangeListener(e -> resizeVisibleGrid());

        gridTable.setCellSelectionEnabled(true);
        gridTable.setRowSelectionAllowed(false);
        gridTable.setColumnSelectionAllowed(false);
        gridTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        gridTable.setRowHeight(24);
        gridTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        gridTable.setDefaultRenderer(Object.class, new MonoRenderer());
        gridTable.setDefaultEditor(Object.class, new OneCharEditor());
        gridTable.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    editCellAtMouse(e);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    paintCellAtMouse(e);
                }
            }
        });
        gridTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    paintCellAtMouse(e);
                }
            }
        });

        JPanel charPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        charPanel.setBorder(BorderFactory.createTitledBorder("Caràcters ràpids"));
        String chars = "# .-HGPDEFWTASBbgCn";
        for (char c : chars.toCharArray()) {
            JButton b = new JButton(c == ' ' ? "espai" : String.valueOf(c));
            b.setMargin(new Insets(2, 6, 2, 6));
            b.addActionListener(e -> paintCharField.setText(c == ' ' ? " " : String.valueOf(c)));
            charPanel.add(b);
        }

        JPanel north = new JPanel(new BorderLayout());
        north.add(top, BorderLayout.NORTH);
        north.add(params, BorderLayout.CENTER);
        north.add(charPanel, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(gridTable), BorderLayout.CENTER);
        pack();
    }

    // Mostra la finestra per triar un JSON.
    private void chooseOpen() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JSON", "json"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile());
        }
    }

    // Obre un fitxer JSON i carrega els mapes.
    private void openFile(File file) {
        try {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            List<MapEntry> maps = GSON.fromJson(json, MAP_LIST_TYPE);
            if (maps == null) maps = new ArrayList<>();
            mapComboModel.removeAllElements();
            for (MapEntry entry : maps) {
                entry.ensureMemoryFromView();
                mapComboModel.addElement(entry);
            }
            currentFile = file;
            setTitle("ASCII Map Studio - " + file.getName());
            if (mapComboModel.getSize() == 0) newMap();
            else mapCombo.setSelectedIndex(0);
            loadSelectedMapToUi();
        } catch (Exception ex) {
            showError("No s'ha pogut obrir el JSON", ex);
        }
    }

    // Guarda els mapes al fitxer actual o demana un nom nou.
    private void save(boolean forceSaveAs) {
        updateCurrentMapFromUi();
        if (currentFile == null || forceSaveAs) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("JSON", "json"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            currentFile = chooser.getSelectedFile();
            if (!currentFile.getName().toLowerCase(Locale.ROOT).endsWith(".json")) {
                currentFile = new File(currentFile.getParentFile(), currentFile.getName() + ".json");
            }
        }
        try {
            List<MapEntry> maps = getAllMapsForSaving();
            Files.writeString(currentFile.toPath(), GSON.toJson(maps, MAP_LIST_TYPE), StandardCharsets.UTF_8);
            setTitle("ASCII Map Studio - " + currentFile.getName());
            JOptionPane.showMessageDialog(this, "Guardat correctament:\n" + currentFile.getAbsolutePath());
        } catch (Exception ex) {
            showError("No s'ha pogut guardar el JSON", ex);
        }
    }

    // Agafa tots els mapes preparats per guardar.
    private List<MapEntry> getAllMapsForSaving() {
        List<MapEntry> maps = new ArrayList<>();
        for (int i = 0; i < mapComboModel.getSize(); i++) {
            MapEntry e = mapComboModel.getElementAt(i);
            e.syncViewFromVisibleMemory(); // només es guarda la graella visible actual
            maps.add(e.copyForJson());
        }
        return maps;
    }

    // Crea un mapa nou de prova.
    private void newMap() {
        MapEntry entry = new MapEntry("map", nextLevel(), Arrays.asList(
                "###############################",
                "#                             #",
                "#             P               #",
                "#.............................#",
                "###############################"
        ));
        entry.ensureMemoryFromView();
        mapComboModel.addElement(entry);
        mapCombo.setSelectedItem(entry);
        loadSelectedMapToUi();
    }

    // Calcula el següent nivell lliure.
    private int nextLevel() {
        int max = 0;
        for (int i = 0; i < mapComboModel.getSize(); i++) max = Math.max(max, mapComboModel.getElementAt(i).level);
        return max + 1;
    }

    // Duplica el mapa seleccionat.
    private void duplicateMap() {
        MapEntry selected = selectedMap();
        if (selected == null) return;
        updateCurrentMapFromUi();
        MapEntry copy = selected.deepCopy();
        copy.level = nextLevel();
        mapComboModel.addElement(copy);
        mapCombo.setSelectedItem(copy);
    }

    // Elimina el mapa seleccionat.
    private void deleteSelectedMap() {
        MapEntry selected = selectedMap();
        if (selected == null) return;
        if (mapComboModel.getSize() <= 1) {
            JOptionPane.showMessageDialog(this, "Ha d'existir almanco un mapa.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Eliminar " + selected + "?", "Confirmació", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            mapComboModel.removeElement(selected);
            mapCombo.setSelectedIndex(0);
            loadSelectedMapToUi();
        }
    }

    // Retorna el mapa triat al desplegable.
    private MapEntry selectedMap() {
        return (MapEntry) mapCombo.getSelectedItem();
    }

    // Posa les dades del mapa seleccionat a la pantalla.
    private void loadSelectedMapToUi() {
        if (loadingUi) return;
        MapEntry selected = selectedMap();
        if (selected == null) return;
        loadingUi = true;
        selected.ensureMemoryFromView();
        typeField.setText(selected.type == null ? "map" : selected.type);
        levelSpinner.setValue(selected.level);
        widthSpinner.setValue(selected.visibleWidth);
        heightSpinner.setValue(selected.visibleHeight);
        gridModel.setMap(selected);
        configureColumnWidths();
        loadingUi = false;
    }

    // Copia els valors de la pantalla al mapa actual.
    private void updateCurrentMapFromUi() {
        if (loadingUi) return;
        MapEntry selected = selectedMap();
        if (selected == null) return;
        selected.type = typeField.getText().isBlank() ? "map" : typeField.getText().trim();
        selected.level = (Integer) levelSpinner.getValue();
        selected.visibleWidth = (Integer) widthSpinner.getValue();
        selected.visibleHeight = (Integer) heightSpinner.getValue();
        selected.ensureCapacity(selected.visibleWidth, selected.visibleHeight);
        mapCombo.repaint();
    }

    // Canvia la mida visible de la graella.
    private void resizeVisibleGrid() {
        if (loadingUi) return;
        MapEntry selected = selectedMap();
        if (selected == null) return;
        updateCurrentMapFromUi();
        selected.ensureCapacity(selected.visibleWidth, selected.visibleHeight);
        gridModel.fireTableStructureChanged();
        configureColumnWidths();
    }

    // Dona la mateixa amplada a totes les columnes.
    private void configureColumnWidths() {
        for (int i = 0; i < gridTable.getColumnModel().getColumnCount(); i++) {
            gridTable.getColumnModel().getColumn(i).setPreferredWidth(24);
            gridTable.getColumnModel().getColumn(i).setMinWidth(24);
        }
    }

    // Obre una casella per escriure-hi quan fas clic esquerre.
    private void editCellAtMouse(MouseEvent e) {
        int row = gridTable.rowAtPoint(e.getPoint());
        int col = gridTable.columnAtPoint(e.getPoint());
        if (row < 0 || col < 0) return;

        gridTable.changeSelection(row, col, false, false);
        if (gridTable.editCellAt(row, col, e)) {
            Component editor = gridTable.getEditorComponent();
            if (editor != null) {
                editor.requestFocusInWindow();
                if (editor instanceof JTextField textField) {
                    textField.selectAll();
                }
            }
        }
    }

    // Pinta una casella amb el caràcter triat quan fas clic dret o arrossegues.
    private void paintCellAtMouse(MouseEvent e) {
        if (!paintMode.isSelected()) return;
        if (gridTable.isEditing()) gridTable.getCellEditor().stopCellEditing();

        int row = gridTable.rowAtPoint(e.getPoint());
        int col = gridTable.columnAtPoint(e.getPoint());
        if (row < 0 || col < 0) return;
        String s = paintCharField.getText();
        char c = s == null || s.isEmpty() ? ' ' : s.charAt(0);
        gridModel.setValueAt(String.valueOf(c), row, col);
    }

    // Mostra un missatge quan hi ha un error.
    private void showError(String msg, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, msg + ":\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static class MapEntry {
        public String type;
        public int level;
        public List<String> view;

        transient char[][] memory;
        transient int visibleWidth;
        transient int visibleHeight;

        // Crea un mapa amb tipus, nivell i línies de text.
        public MapEntry(String type, int level, List<String> view) {
            this.type = type;
            this.level = level;
            this.view = new ArrayList<>(view);
            ensureMemoryFromView();
        }

        // Passa la vista de text a memòria editable.
        public void ensureMemoryFromView() {
            if (view == null) view = new ArrayList<>();
            visibleHeight = Math.max(1, view.size());
            visibleWidth = 1;
            for (String row : view) visibleWidth = Math.max(visibleWidth, row == null ? 0 : row.length());
            memory = new char[visibleHeight][visibleWidth];
            for (int r = 0; r < visibleHeight; r++) {
                Arrays.fill(memory[r], ' ');
                String row = view.get(r) == null ? "" : view.get(r);
                for (int c = 0; c < row.length(); c++) memory[r][c] = row.charAt(c);
            }
        }

        // Fa la memòria més gran si fa falta.
        public void ensureCapacity(int width, int height) {
            if (memory == null) ensureMemoryFromView();
            int newH = Math.max(height, memory.length);
            int newW = Math.max(width, memory.length == 0 ? 1 : memory[0].length);
            if (newH == memory.length && newW == memory[0].length) return;
            char[][] next = new char[newH][newW];
            for (char[] row : next) Arrays.fill(row, ' ');
            for (int r = 0; r < memory.length; r++) {
                System.arraycopy(memory[r], 0, next[r], 0, memory[r].length);
            }
            memory = next;
        }

        // Passa la memòria visible a línies de text.
        public void syncViewFromVisibleMemory() {
            ensureCapacity(visibleWidth, visibleHeight);
            List<String> out = new ArrayList<>();
            for (int r = 0; r < visibleHeight; r++) {
                out.add(new String(memory[r], 0, visibleWidth));
            }
            view = out;
        }

        // Crea una còpia neta per escriure-la al JSON.
        public MapEntry copyForJson() {
            syncViewFromVisibleMemory();
            return new MapEntry(type, level, view);
        }

        // Crea una còpia completa del mapa.
        public MapEntry deepCopy() {
            syncViewFromVisibleMemory();
            MapEntry copy = new MapEntry(type, level, view);
            copy.visibleWidth = visibleWidth;
            copy.visibleHeight = visibleHeight;
            copy.ensureCapacity(memory[0].length, memory.length);
            for (int r = 0; r < memory.length; r++) {
                System.arraycopy(memory[r], 0, copy.memory[r], 0, memory[r].length);
            }
            return copy;
        }

        // Text que es veu al desplegable de mapes.
        @Override public String toString() {
            return "level " + level + " (" + (type == null ? "map" : type) + ")";
        }
    }

    private class GridModel extends AbstractTableModel {
        private MapEntry map;

        // Assigna quin mapa mostra la taula.
        public void setMap(MapEntry map) {
            this.map = map;
            fireTableStructureChanged();
        }

        // Nombre de files visibles.
        @Override public int getRowCount() {
            return map == null ? 0 : map.visibleHeight;
        }

        // Nombre de columnes visibles.
        @Override public int getColumnCount() {
            return map == null ? 0 : map.visibleWidth;
        }

        // Retorna el caràcter d’una casella.
        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            if (map == null) return " ";
            map.ensureCapacity(map.visibleWidth, map.visibleHeight);
            return String.valueOf(map.memory[rowIndex][columnIndex]);
        }

        // Canvia el caràcter d’una casella.
        @Override public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (map == null) return;
            String s = value == null ? " " : value.toString();
            char c = s.isEmpty() ? ' ' : s.charAt(0);
            map.memory[rowIndex][columnIndex] = c;
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        // Permet editar les caselles.
        @Override public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        // Nom de cada columna.
        @Override public String getColumnName(int column) {
            return String.valueOf(column + 1);
        }
    }

    private static class MonoRenderer extends DefaultTableCellRenderer {
        // Dibuixa cada casella centrada i amb lletra monoespaiada.
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
            return label;
        }
    }

    private static class OneCharEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextField field = new JTextField();

        // Prepara l’editor d’un sol caràcter.
        public OneCharEditor() {
            field.setHorizontalAlignment(SwingConstants.CENTER);
            field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
            field.setDocument(new OneCharDocument());
        }

        // Retorna només el primer caràcter escrit.

        // Només deixa començar a escriure amb el clic esquerre.
        @Override public boolean isCellEditable(EventObject e) {
            if (e instanceof MouseEvent mouseEvent) {
                return SwingUtilities.isLeftMouseButton(mouseEvent);
            }
            return false;
        }

        @Override public Object getCellEditorValue() {
            String s = field.getText();
            return s == null || s.isEmpty() ? " " : s.substring(0, 1);
        }

        // Mostra el camp de text dins la casella.
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            field.setText(value == null ? " " : value.toString());
            SwingUtilities.invokeLater(field::selectAll);
            return field;
        }
    }

    private static class OneCharDocument extends javax.swing.text.PlainDocument {
        // Deixa escriure només un caràcter.
        @Override public void insertString(int offset, String str, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
            if (str == null || str.isEmpty()) return;
            remove(0, getLength());
            super.insertString(0, str.substring(0, 1), attr);
        }
    }

    @FunctionalInterface
    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent e);
        @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { update(e); }
        @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { update(e); }
    }
}
