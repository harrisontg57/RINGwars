import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public class COREapp extends JFrame {

    private JPanel bottomPanel; // Panel to hold agent buttons
    private JLabel worldSize;
    private JLabel perTurn;
    private JLabel maxSoldiers;
    private JLabel visRange;
    private JLabel startCLabel;
    private JLabel singleAgentMode;
    private JLabel absorbModeLabel;
    private JLabel compModeLabel;
    public int sSpeed;
    public int gpturn;
    public int wSize;
    public int startcount;
    public int maxNumSoldiers;
    public int visibility_range;
    private JPanel topDisplayPanel; //Panel to hold Reload button
    private JPanel simdisplayPanel;
    public int step; //sim step
    public int displayStep; //The display step, might go back and forth
    //sim super step can be gotten from the sim at any time.
    public int displaySuperStep; //The step of the sim being displayed..corresponds to the super step
    public Simulation sim;
    public Set<Agent_Details> agent_set; //all the agents added to the app
    public Agent_Details[] active_agents; //just the 2 agents who play against eachother
    private Timer timer;
    public HashMap<String, Agent_Details> agentLookup;
    public HashMap<Color, Integer> faceLookup;
    public int aCount;
    public int startingSuperStep;
    public HashMap<Integer, Integer> superStep2Step;
    public Boolean debug_mode;
    public Boolean singleMode;
    public Boolean absorbMode;
    public int growth_mode;
    private JButton addAgentButton;
    private JButton worldSizeButton;
    private JButton growthPerTurnButton;
    private JButton startingFerniesButton;
    private JButton maxPerTileButton;
    private JButton visibilityRangeButton;
    private JButton changeNodeOwnerShiftBonusButton;
    private JButton singleAgentModeButton;
    private JButton absorbModeButton;
    private JButton saveSettingsButton;
    private JButton loadSettingsButton;

    public COREapp() {
        setTitle("RINGwars Visualizer --v2.0");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //set default world values...size, fernies per round, etc
        this.wSize = 20;
        this.sSpeed = 100;
        this.gpturn = 10;
        this.visibility_range = 5;
        this.maxNumSoldiers = 10000;
        this.startcount = 75;
        this.step = 0;
        this.displayStep = 0;
        this.displaySuperStep = 0;
        this.startingSuperStep = 0;
        this.agent_set = new HashSet<>();
        this.active_agents = new Agent_Details[2];
        this.agentLookup = new HashMap<>();
        this.faceLookup = new HashMap<>();
        this.superStep2Step = new HashMap<>();
        this.debug_mode = false;
        this.singleMode = false;
        this.absorbMode = true;
        this.growth_mode = 1;
        Random random = new Random();
        this.aCount = random.nextInt(4);
        initializeUI();
    }

    private void initializeUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        //add image
        JPanel startPanel = new JPanel(new BorderLayout());
        ImageIcon flavourImage = new ImageIcon("src/RAMfight.png","this is a caption");
        Image tmpImage = flavourImage.getImage(); // transform it 
        Image flavour = tmpImage.getScaledInstance(450, 450,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
        ImageIcon borderIcon = new ImageIcon("src/LoadScreenBorder.png","this is a caption"); 
        flavourImage = new ImageIcon(flavour);
        JLabel flavourPanel = new JLabel(borderIcon);
        
        
        //JLabel welcome = new JLabel("Welcome to COREwars!");
        JPanel startCenter = new JPanel();
        //startCenter.add(welcome,BorderLayout.CENTER);
        startPanel.add(startCenter,BorderLayout.NORTH);
        startPanel.add(flavourPanel,BorderLayout.CENTER);
        tabbedPane.addTab("Welcome", startPanel);

        // ---------------------------------------------------------------------------------------------------
        //                                        Settings Panel V1
        // ---------------------------------------------------------------------------------------------------
        // Menu Panel with JMenuBar
        JPanel menuPanel = new JPanel(new BorderLayout());
        JMenuBar menuBar = new JMenuBar();
        //JMenu menu = new JMenu();
        //ImageIcon settingsIcon = new ImageIcon("src/SimSettings.gif","this is a caption");
        //menu.setIcon(settingsIcon);
        JLabel agentListLabel = new JLabel();
        ImageIcon agentListIconOrg = new ImageIcon("src/agentList.png");
        Image agentListIconOrgScaledImage = agentListIconOrg.getImage().getScaledInstance(140, 42, Image.SCALE_SMOOTH);
        ImageIcon agentListIconResized = new ImageIcon(agentListIconOrgScaledImage);
        agentListLabel.setIcon(agentListIconResized);

        /*JMenuItem sizeItem = new JMenuItem("Set Ring Size");
        sizeItem.addActionListener(e -> {
            int worldsize = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter new world size:"));
            // Implement size change logic here
            wSize = worldsize;
            updateWorldSize(worldsize);
        });
        */

        /*
        JMenuItem growthItem = new JMenuItem("Set Fernie Growth per Turn");
        growthItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Percent Growth per Turn:"));
            // Implement size change logic here
            gpturn = tmp;
            updateGPTurn(tmp);
        });
        */

        /*
        JMenuItem startItem = new JMenuItem("Set Starting Fernies");
        startItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Starting Fernies:"));
            // Implement size change logic here
            startcount = tmp;
            updateStartCount(tmp);
        });
        */

        /*
        JMenuItem maxItem = new JMenuItem("Set Max per Tile");
        maxItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Maximum:"));
            // Implement size change logic here
            maxNumSoldiers = tmp;
            updateMax(tmp);
        });
        */

        /*
        JMenuItem visItem = new JMenuItem("Set Visibility Range");
        visItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Visibility Range:"));
            // Implement size change logic here
            visibility_range = tmp;
            updateVis(tmp);
        });
        */

        /*
        JMenuItem singleAgentModeItem = new JMenuItem("Set to Single Agent Mode");
        singleAgentModeItem.addActionListener(e -> {
            if (this.singleMode) {
                this.singleMode = false;
                singleAgentMode.setText("Multi Agent Mode");
                singleAgentModeItem.setText("Set to Single Agent Mode");
            } else {
                this.singleMode = true;
                singleAgentMode.setText("Single Agent Mode");
                singleAgentModeItem.setText("Set to Multi Agent Mode");
            }
            Component temp = singleAgentMode.getParent();
            temp.revalidate();
            temp.repaint();
        });
        singleAgentModeItem.setToolTipText("For experimenting with agent strategies.");
        */

        /*
        JMenuItem absorbModeItem = new JMenuItem("Set to Cancel Mode");
        absorbModeItem.addActionListener(e -> {
            if (!this.absorbMode) {
                this.absorbMode = true;
                absorbModeItem.setText("Set to Cancel Mode");
                absorbModeLabel.setText("Currently in Absorb Mode");
            } else {
                this.absorbMode = false;
                absorbModeItem.setText("Set to Absorb Mode");
                absorbModeLabel.setText("Currently in Cancel Mode");
            }
            Component temp = absorbModeItem.getParent();
            temp.revalidate();
            temp.repaint();
        });
        absorbModeItem.setToolTipText("Determines whether edge battles resolve in aborbing conquered fernies or deleting them from the conquerors count");
        */

        //JMenuItem addAgentItem = new JMenuItem("Add Agent");
        //addAgentItem.addActionListener(e -> createAddAgentPopup());

        /* 
        JMenuItem saveSettings = new JMenuItem("Save Settings");
        saveSettings.addActionListener(e -> {
            // Create popup
            JDialog dialog = new JDialog(this, "Save Settings", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(300, 150);
            dialog.setLocationRelativeTo(this);

            // Create panel with label + text field
            JPanel savePanelPopup = new JPanel(new FlowLayout());
            JLabel savePanelLabel = new JLabel("Save Name:");
            JTextField savePanelField = new JTextField(15);
            savePanelPopup.add(savePanelLabel);
            savePanelPopup.add(savePanelField);

            // Create Save button
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(ae -> {
                String save_name = savePanelField.getText();
                saveSettings(save_name + ".json");
                dialog.dispose();
            });

            // Add components to dialog
            dialog.add(savePanelPopup, BorderLayout.CENTER);
            dialog.add(saveButton, BorderLayout.SOUTH);

            dialog.setVisible(true);
        });
        */
        
        /*
        JMenuItem loadSettings = new JMenuItem("Load Settings");
        loadSettings.addActionListener(e -> {
            File folder = new File("saves/agents/");
            if (!folder.exists() || !folder.isDirectory()) {
                JOptionPane.showMessageDialog(this, "No 'saves/agents/' folder found.");
                return;
            }

            // Filter for .json files
            String[] saveFiles = folder.list((dir, name) -> name.endsWith(".json"));

            if (saveFiles == null || saveFiles.length == 0) {
                JOptionPane.showMessageDialog(this, "No saved settings found in 'saves/agents/' folder.");
                return;
            }

            // Show popup with a combo box of files
            JComboBox<String> fileList = new JComboBox<>(saveFiles);
            int result = JOptionPane.showConfirmDialog(
                this,
                fileList,
                "Choose Save File",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String selectedFile = (String) fileList.getSelectedItem();
                loadSettings(selectedFile);  // <- Load selected file
            }
        });
        */

        //menu.add(sizeItem);
        //menu.add(growthItem);
        //menu.add(startItem);
        //menu.add(maxItem);
        //menu.add(visItem);
        //menu.add(singleAgentModeItem);
        //menu.add(absorbModeItem);
        //menu.add(addAgentItem);
        //menu.add(saveSettings);
        //menu.add(loadSettings);
        //menuBar.add(menu);
        menuBar.add(agentListLabel);
        menuPanel.add(menuBar, BorderLayout.NORTH);
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        menuBar.add(bottomPanel, BorderLayout.SOUTH);

        //JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        //worldSize = new JLabel("World Size: 20");
        //perTurn = new JLabel("Percent Growth per Turn: 10");
        //simSpeed = new JLabel("Simulation Play Speed: 100 ms");
        //visRange = new JLabel("Visibility Range: 5");
        //maxSoldiers = new JLabel("Max Fernies per Node: 10000");
        //startCLabel = new JLabel("Starting Fernies: 75");
        //singleAgentMode = new JLabel("Multi Agent Mode");
        //absorbModeLabel = new JLabel("Currently in Absorb Mode");
        //compModeLabel = new JLabel("Experimental Mode");
        //JLabel newLineHolder = new JLabel("\n");
        //settingsPanel.setLayout(new GridLayout(0, 1));
        //,BorderLayout.CENTER
        //JPanel settingsHolder = new JPanel(new FlowLayout(FlowLayout.CENTER));
        //settingsPanel.add(newLineHolder);
        //settingsPanel.add(new JLabel("\n"));
        //settingsPanel.add(new JLabel("\n"));
        //settingsPanel.add(worldSize);
        //settingsPanel.add(new JLabel("\n"));
        //settingsPanel.add(perTurn);
        //settingsPanel.add(new JLabel("\n"));
        //settingsPanel.add(visRange);
        //settingsPanel.add(new JLabel("\n"));
        //settingsPanel.add(maxSoldiers);
        //settingsPanel.add(new JLabel("\n"));
        //settingsPanel.add(startCLabel);
        //settingsPanel.add(new JLabel("\n"));
        //settingsPanel.add(singleAgentMode);
        //settingsPanel.add(new JLabel("\n"));
        //settingsPanel.add(absorbModeLabel);
        //settingsPanel.add(new JLabel("\n"));
        //settingsPanel.add(compModeLabel);
        //settingsPanel.add(new JLabel("\n"));

        // ---------------------------------------------------------------------------------------------------
        //                                        Settings Panel V2
        // ---------------------------------------------------------------------------------------------------
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel settingsPanel_row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel settingsPanel_row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel settingsPanel_row3 = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // --> ADD AGENT
        addAgentButton = new JButton("<html><center>ADD AGENT</center></html>");
        styleSettingButton(addAgentButton);
        addAgentButton.addActionListener(e -> createAddAgentPopup());
        settingsPanel_row1.add(addAgentButton);

        // --> CHANGE RING SIZE
        worldSizeButton = new JButton("<html><center>CHANGE RING SIZE<br>(20)</center></html>");
        styleSettingButton(worldSizeButton);
        worldSizeButton.addActionListener(e -> {
            int worldsize = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter new world size:"));
            wSize = worldsize;
            worldSizeButton.setText("<html><center>CHANGE RING SIZE<br>(" + worldsize + ")</center></html>");
        });
        settingsPanel_row1.add(worldSizeButton);

        // --> CHANGE FERNIE GROWTH PER TURN
        growthPerTurnButton = new JButton("<html><center>CHANGE FERNIE GROWTH PERCENT PER TURN<br>(10)</center></html>");
        styleSettingButton(growthPerTurnButton);
        growthPerTurnButton.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Percent Growth per Turn:"));
            gpturn = tmp;
            growthPerTurnButton.setText("<html><center>CHANGE FERNIE GROWTH PER TURN<br>(" + tmp + ")</center></html>");
        });
        settingsPanel_row1.add(growthPerTurnButton);
        
        // --> CHANGE STARTING FERNIES
        startingFerniesButton = new JButton("<html><center>CHANGE STARTING FERNIES<br>(75)</center></html>");
        styleSettingButton(startingFerniesButton);
        startingFerniesButton.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Starting Fernies:"));
            startcount = tmp;
            startingFerniesButton.setText("<html><center>CHANGE STARTING FERNIES<br>(" + tmp + ")</center></html>");
        });
        settingsPanel_row1.add(startingFerniesButton);

        // --> CHANGE MAX FERNIES PER TILE
        maxPerTileButton = new JButton("<html><center>CHANGE MAX FERNIES PER NODE<br>(10000)</center></html>");
        styleSettingButton(maxPerTileButton);
        maxPerTileButton.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Maximum:"));
            maxNumSoldiers = tmp;
            maxPerTileButton.setText("<html><center>CHANGE MAX FERNIES PER NODE<br>(" + tmp + ")</center></html>");
        });
        settingsPanel_row2.add(maxPerTileButton);

        // --> CHANGE VISIBILITY RANGE
        visibilityRangeButton = new JButton("<html><center>CHANGE VISIBILITY RANGE<br>(5)</center></html>");
        styleSettingButton(visibilityRangeButton);
        visibilityRangeButton.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter New Visibility Range:"));
            visibility_range = tmp;
            visibilityRangeButton.setText("<html><center>CHANGE VISIBILITY RANGE<br>(" + tmp + ")</center></html>");
        });
        settingsPanel_row2.add(visibilityRangeButton);

        // --> CHANGE NODE OWNER SHIFT BONUS
        changeNodeOwnerShiftBonusButton = new JButton("<html><center>CHANGE NODE OWNERSHIP BONUS<br>(10)</center></html>");
        styleSettingButton(changeNodeOwnerShiftBonusButton);
        settingsPanel_row2.add(changeNodeOwnerShiftBonusButton);

        // --> SET SINGLE AGENT MODE
        singleAgentModeButton = new JButton("<html><center>SET TO SINGLE AGENT MODE</center></html>");
        styleSettingButton(singleAgentModeButton);
        singleAgentModeButton.addActionListener(e -> {
            if (this.singleMode) {
                this.singleMode = false;
                singleAgentModeButton.setText("<html><center>SET TO SINGLE AGENT MODE</center></html>");
            } else {
                this.singleMode = true;
                singleAgentModeButton.setText("<html><center>SET TO MULTI AGENT MODE</center></html>");
            }
            Component temp = singleAgentModeButton.getParent();
            temp.revalidate();
            temp.repaint();
        });
        //settingsPanel_row2.add(singleAgentModeButton);

        // --> SET ABSORB MODE
        absorbModeButton = new JButton("<html><center>SET TO ABSORB MODE</center></html>");
        styleSettingButton(absorbModeButton);
        absorbModeButton.addActionListener(e -> {
            if (!this.absorbMode) {
                this.absorbMode = true;
                absorbModeButton.setText("<html><center>SET TO ABSORB MODE</center></html>");
            } else {
                this.absorbMode = false;
                absorbModeButton.setText("<html><center>SET TO CANCEL MODE</center></html>");
            }
            Component temp = absorbModeButton.getParent();
            temp.revalidate();
            temp.repaint();
        });
        //settingsPanel_row2.add(absorbModeButton);

        // --> SAVE SETTINGS
        saveSettingsButton = new JButton("<html><center>SAVE SETTINGS</center></html>");
        styleSettingButton(saveSettingsButton);
        saveSettingsButton.addActionListener(e -> {
            // Create popup
            JDialog dialog = new JDialog(this, "Save Settings", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(300, 150);
            dialog.setLocationRelativeTo(this);

            // Create panel with label + text field
            JPanel savePanelPopup = new JPanel(new FlowLayout());
            JLabel savePanelLabel = new JLabel("Save Name:");
            JTextField savePanelField = new JTextField(15);
            savePanelPopup.add(savePanelLabel);
            savePanelPopup.add(savePanelField);

            // Create Save button
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(ae -> {
                String save_name = savePanelField.getText();
                saveSettings(save_name + ".json");
                dialog.dispose();
            });

            // Add components to dialog
            dialog.add(savePanelPopup, BorderLayout.CENTER);
            dialog.add(saveButton, BorderLayout.SOUTH);

            dialog.setVisible(true);
        });
        settingsPanel_row2.add(saveSettingsButton);

        // --> LOAD SETTINGS
        loadSettingsButton = new JButton("<html><center>LOAD SETTINGS</center></html>");
        loadSettingsButton.addActionListener(e -> {
            File folder = new File("saves/agents/");
            if (!folder.exists() || !folder.isDirectory()) {
                JOptionPane.showMessageDialog(this, "No 'saves/agents/' folder found.");
                return;
            }

            // Filter for .json files
            String[] saveFiles = folder.list((dir, name) -> name.endsWith(".json"));

            if (saveFiles == null || saveFiles.length == 0) {
                JOptionPane.showMessageDialog(this, "No saved settings found in 'saves/agents/' folder.");
                return;
            }

            // Show popup with a combo box of files
            JComboBox<String> fileList = new JComboBox<>(saveFiles);
            int result = JOptionPane.showConfirmDialog(
                this,
                fileList,
                "Choose Save File",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String selectedFile = (String) fileList.getSelectedItem();
                loadSettings(selectedFile);  // <- Load selected file
            }
        });
        styleSettingButton(loadSettingsButton);
        settingsPanel_row3.add(loadSettingsButton);

        Box settingsPanel_container = Box.createVerticalBox();
        settingsPanel_container.add(settingsPanel_row1);
        settingsPanel_container.add(settingsPanel_row2);
        settingsPanel_container.add(settingsPanel_row3);

        settingsPanel.add(settingsPanel_container);

        JPanel settingsHolder = new JPanel(new FlowLayout(FlowLayout.CENTER));

        settingsHolder.add(settingsPanel);
        menuPanel.add(settingsHolder);

        tabbedPane.addTab("Settings", menuPanel);
        
        // Display Panel
        JPanel displayPanel = new JPanel(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false); // Prevents the toolbar from being moved
        ImageIcon bIcon = new ImageIcon("src/back.gif","this is a caption");
        JButton backButton = new JButton(bIcon);
        ImageIcon playIcon = new ImageIcon("src/play.gif","this is a caption");
        JButton startButton = new JButton(playIcon);
        ImageIcon pauseIcon = new ImageIcon("src/pause.gif","this is a caption");
        JButton stopButton = new JButton(pauseIcon);
        ImageIcon fIcon = new ImageIcon("src/forward.gif","this is a caption");
        JButton forwardButton = new JButton(fIcon);
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepForward();
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSimulation();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSimulation();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepBackward();
            }
        });


        toolBar.add(backButton);
        toolBar.add(stopButton);
        toolBar.add(startButton);
        toolBar.add(forwardButton);
        
        ImageIcon reloadIcon = new ImageIcon("src/reload_button.gif","this is a caption");
        //Image image = reloadIcon.getImage(); // transform it 
        //Image newimg = image.getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        //reloadIcon = new ImageIcon(image);
        JButton reloadButton = new JButton(reloadIcon);
        reloadButton.setToolTipText("RELOAD");
        reloadButton.addActionListener(e -> reloadSim());

        reloadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED)); // Remove borders
        reloadButton.setFocusPainted(false);  // Disable focus painting
        reloadButton.setContentAreaFilled(false);  // Remove background painting
        //reloadButton.setOpaque(true);  // Enable opaque to use the background color
        
        
        //reloadButton.setForeground(UIManager.getColor("Menu.foreground"));
        //reloadButton.setFont(UIManager.getFont("Menu.font"));

        ImageIcon pSettingsIcon = new ImageIcon("src/playSettings.gif","this is a caption");
        JPanel simSetButton = new JPanel();
        
        
        simSetButton.setToolTipText("Simulation Control Buttons");
        JMenuBar simMenuBar = new JMenuBar();
        
        JMenu simButtonMenu = new JMenu();
        simButtonMenu.setToolTipText("Speed and Step Navigation");
        simButtonMenu.setFocusPainted(true);
        simButtonMenu.setContentAreaFilled(false);
        simButtonMenu.setIcon(pSettingsIcon);
        simButtonMenu.setOpaque(true);
        simButtonMenu.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        JMenuItem preCalcItem = new JMenuItem("PreCalc Next 10 Steps");
        preCalcItem.addActionListener(e -> {
            preCalcSim(10);
        });
        preCalcItem.setToolTipText("Run the simulation for 10 steps");
        JMenuItem preCalcNItem = new JMenuItem("PreCalc Next N Steps");
        preCalcNItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Steps to Precompute:"));
            preCalcSim(tmp);
        });
        preCalcNItem.setToolTipText("Run the simulation for N steps.  CAREFUL! Test how long 10 steps take before you try 1000 etc..");
        JMenuItem returnItem = new JMenuItem("Return to Start of Sim");
        returnItem.addActionListener(e -> {
            returnToStep(1);
        });
        JMenuItem returnNItem = new JMenuItem("Return to Step N");
        returnNItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "Return to Step:"));
            returnToStep(tmp);
        });
        returnNItem.setToolTipText("Return the display to step N");
        JMenuItem setSpeedItem = new JMenuItem("Set Playback Speed");
        setSpeedItem.addActionListener(e -> {
            int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "New Sim Speed (ms):"));
            this.sSpeed = tmp;
        });
        JMenuItem debug = new JMenuItem("Set to Debug Mode");
        debug.addActionListener(e -> {
            if (this.debug_mode) {
                this.debug_mode = false;
                debug.setText("Set to Debug Mode");
            } else {
                this.debug_mode = true;
                debug.setText("Set to Normal Mode");
            }
            simdisplayPanel.repaint();
        });
        debug.setToolTipText("Debug mode shows you information about agent actions and gives you node location numbers");
        //simButtonMenu.add(preCalcItem);
        //simButtonMenu.add(preCalcNItem);
        simButtonMenu.add(returnItem);
        simButtonMenu.add(returnNItem);
        //simButtonMenu.add(setSpeedItem);
        simButtonMenu.add(debug);
        simMenuBar.add(simButtonMenu);
        simSetButton.add(simMenuBar);

        topDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,0));
        topDisplayPanel.add(reloadButton);
        topDisplayPanel.add(simSetButton);
        displayPanel.add(topDisplayPanel,BorderLayout.NORTH);
        simdisplayPanel = new DisplayPanel();
        simdisplayPanel.setBackground(Color.WHITE);
        displayPanel.add(simdisplayPanel,FlowLayout.CENTER);

        // Centering ToolBar
        JPanel toolBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        toolBarPanel.add(toolBar);
        displayPanel.add(toolBarPanel, BorderLayout.SOUTH); // Add centered toolbar to the bottom

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 100);
        speedSlider.setMajorTickSpacing(200);
        speedSlider.setMinorTickSpacing(50);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> {
            //int tmp = Integer.parseInt(JOptionPane.showInputDialog(this, "New Sim Speed (ms):"));
            JSlider source = (JSlider)e.getSource();
            int tmp = (int)source.getValue();
            this.sSpeed = tmp;
        });

        JLabel speedLabel = new JLabel("Set Play Speed (ms): ");
        topDisplayPanel.add(speedLabel);
        topDisplayPanel.add(speedSlider);
        
        tabbedPane.addTab("Display", displayPanel);

        // Add the tabbed pane to the frame
        add(tabbedPane);
    }

    private void reloadSim() {
        ArrayList<String> ag_names = new ArrayList<>();
        ArrayList<Agent_Details> myAgents = new ArrayList<>();
        //int tmp_i = 0;
        for (Agent_Details adetails : agent_set) {
            //ADD SOMETHING HERE THAT CHECKS WHETHER THE AGENT IS ACTIVE OR NOT
            //ONLY ACTIVE AGENTS WILL BE ADDED
            myAgents.add(adetails);
            ag_names.add(adetails.locname);
            File theDir = new File(adetails.locname);
            if (!theDir.exists()){
                theDir.mkdirs();
            }
        }
        this.active_agents = myAgents.toArray(new Agent_Details[0]);
        String[] agArray = new String[ ag_names.size() ];
        ag_names.toArray( agArray );
        //this.sim = new Simulation(this.wSize,agArray,agentLookup,gpturn,maxNumSoldiers,startcount,visibility_range,growth_mode,absorbMode);
        this.sim = new Simulation(this.wSize, this.active_agents, maxNumSoldiers, startcount, visibility_range, gpturn, 5,agentLookup);
        this.step = 1;
        this.displayStep = 1;
        //this.displaySuperStep = sim.superStep;
        //this.startingSuperStep = sim.superStep;
        //this.superStep2Step.put(displaySuperStep,displayStep);
        //System.out.println("Starting superstep: " + this.startingSuperStep);
        simdisplayPanel.repaint();
    }

    private void returnToStep(int n) {
        this.displayStep = n;
        if (n == 1 | n == 0) {
            this.displaySuperStep = this.startingSuperStep;
        } else {
            this.displaySuperStep = this.startingSuperStep + n*agent_set.size();
        }
        simdisplayPanel.repaint();
    }

    private void preCalcSim(int n) {
        int currentDisplayStep = this.displayStep;
        int currentDisplaySuperStep = this.displaySuperStep;
        int ac = agent_set.size();
        for (int i = 0; i < n*ac; i++) {
            this.updateSimulationNoDisplay();
        }
        this.displayStep = currentDisplayStep;
        this.displaySuperStep = currentDisplaySuperStep;
        simdisplayPanel.repaint();   
    }

    private void startSimulation() {
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(sSpeed, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (displayStep < sim.step) {
                    displayStep++;
                    //displayStep = superStep2Step.get(displaySuperStep);
                    simdisplayPanel.repaint();
                    return;
                }
                updateSimulation();
            }
        });
        timer.start();
    }

    private void stopSimulation() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void stepForward() {
        //needs to check if display step < step in which case updateSim isn't called but display step moves up
        if (displayStep < sim.step) {
            displayStep++;
            //displayStep = this.superStep2Step.get(displaySuperStep);
            simdisplayPanel.repaint();
            return;
        }
        updateSimulation(); // Placeholder
    }
    
    private void stepBackward() {
        // Add code to step backward in the simulation
        //move back a step
        //displaySuperStep--;
        //displayStep = this.superStep2Step.get(displaySuperStep);
        displayStep--;
        if (displayStep < 0) {
            displayStep = 0;
            displayStep = 1;
        }
        simdisplayPanel.repaint();
    }

    private void updateWorldSize(int size) {
        worldSize.setText("World Size: "+String.valueOf(size));
        Component temp = worldSize.getParent();
        temp.revalidate();
        temp.repaint();
    }

    private void updateGPTurn(int size) {
        perTurn.setText("Percent Growth per Turn: "+String.valueOf(size));
        Component temp = perTurn.getParent();
        temp.revalidate();
        temp.repaint();
        //still needs to change the sim settings
    }

    private void updateStartCount(int count) {
        startCLabel.setText("Starting Fernies: "+String.valueOf(count));
        Component temp = startCLabel.getParent();
        temp.revalidate();
        temp.repaint();
    }
    
    private void updateMax(int maxx) {
        maxSoldiers.setText("Max Fernies per Node: "+String.valueOf(maxx));
        Component temp = maxSoldiers.getParent();
        temp.revalidate();
        temp.repaint();
    }
    
    private void updateVis(int maxx) {
        visRange.setText("Visibility Range: "+String.valueOf(maxx));
        Component temp = visRange.getParent();
        temp.revalidate();
        temp.repaint();
    }

    private void createAddAgentPopup() {
        JDialog addAgentDialog = new JDialog(this, "Add Agent", true);
        addAgentDialog.setLayout(new BorderLayout());
        addAgentDialog.setSize(500, 600);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField nameField = new JTextField(10);
        JTextField locationField = new JTextField(10);
        JTextField programField = new JTextField("java",10);
        nameField.setToolTipText("Literally your Agent program.  Should be stored locally.");
        locationField.setToolTipText("What folder to store your agent statefiles in.  This will also be the 'name' of your agent displayed in the debug mode");
        programField.setToolTipText("What call procedes your agent program when running it from the command line. e.g. 'java' or 'python3.7' For executables leave this empty.");
        inputPanel.add(new JLabel("Name of Agent Class File:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Where to Store State Files:"));
        inputPanel.add(locationField);
        inputPanel.add(new JLabel("Language Call:"));
        inputPanel.add(programField);
        inputPanel.add(new JLabel("Color:"));

        JColorChooser colorChooser = new JColorChooser();
        colorChooser.setPreviewPanel(new JPanel()); // Remove the preview panel to save space

        addAgentDialog.add(inputPanel, BorderLayout.NORTH);
        addAgentDialog.add(colorChooser, BorderLayout.CENTER);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String location = locationField.getText();
            String language = programField.getText();
            Color color = colorChooser.getColor();
            addAgent(name, location, language, color);
            addAgentButton(name, location, color);
            
            addAgentDialog.dispose(); // Close the dialog after adding the agent
            if(agent_set.size() >= 2) {
                addAgentButton.setEnabled(false);
            } else {
                addAgentButton.setEnabled(true);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        addAgentDialog.add(buttonPanel, BorderLayout.SOUTH);

        addAgentDialog.setVisible(true);
    }

    private void addAgent(String name, String loc, String lang, Color color) {
        //Adds the agent to the list of agents.
        //agent_set;
        
        Agent_Details a_d = new Agent_Details(name,loc,lang,color);
        agentLookup.put(a_d.locname, a_d);
        agent_set.add(a_d);
        faceLookup.put(color,aCount%5);
        aCount++;
    }

    private void addAgentButton(String name, String loc, Color color) {
        JMenuBar agentMenuBar = new JMenuBar();
        JPanel agentButton = new JPanel();
        JMenu agentButtonMenu = new JMenu(name);
        agentButton.setBackground(color);
        //agentButton.setBackground(Color.RED);
        Image myFace;
        int fint = faceLookup.get(color);
        if (fint == 0) {
            myFace = (new ImageIcon("src/cartoonFace.gif")).getImage().getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            //myFace.getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
        } else if (fint == 1) {
            myFace = (new ImageIcon("src/angryEyebrowFace.gif")).getImage().getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
        } else if (fint == 2) {
            myFace = (new ImageIcon("src/angryTongueFace.gif")).getImage().getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
        } else if (fint == 3) {
            myFace = (new ImageIcon("src/dullFace.gif")).getImage().getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
        } else if (fint == 4) {
            myFace = (new ImageIcon("src/crosseyedFace.gif")).getImage().getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
        } else {
            myFace = (new ImageIcon("src/cartoonFace.gif")).getImage().getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
        }
        JMenuItem AgentFaceItem = new JMenuItem(new ImageIcon(myFace));
        AgentFaceItem.setHorizontalAlignment(SwingConstants.CENTER);
        AgentFaceItem.setBackground(color);
        AgentFaceItem.setFocusPainted(false);
        JMenuItem AgentLocItem = new JMenuItem("\\"+loc+"\\");
        AgentLocItem.setHorizontalAlignment(SwingConstants.CENTER);
        JMenuItem removeAgentItem = new JMenuItem("Delete");
        removeAgentItem.setHorizontalAlignment(SwingConstants.CENTER);
        removeAgentItem.addActionListener(e -> {
            Agent_Details a_d = agentLookup.get(loc);
            agent_set.remove(a_d);
            agentLookup.remove(loc);
            //Removes the button from the app.
            JPanel grandparent = bottomPanel;
            grandparent.remove(agentButton);
            grandparent.revalidate();
            grandparent.repaint();

            if(agent_set.size() >= 2) {
                addAgentButton.setEnabled(false);
            } else {
                addAgentButton.setEnabled(true);
            }
        });
        agentButtonMenu.add(AgentFaceItem);
        agentButtonMenu.add(AgentLocItem);
        agentButtonMenu.add(removeAgentItem);
        agentMenuBar.add(agentButtonMenu);
        agentButton.add(agentMenuBar);
        bottomPanel.add(agentButton);
        bottomPanel.revalidate(); // Refresh panel to show new button
    }

    private void saveSettings(String filename) {
        List<Map<String, Object>> agentList = new ArrayList<>();

        for (Agent_Details ad : agent_set) {
            Map<String, Object> agentMap = new HashMap<>();
            agentMap.put("filename", ad.filename);
            agentMap.put("locname", ad.locname);
            agentMap.put("lang", ad.lang);
            Color c = ad.color;
            String hexColor = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
            agentMap.put("color", hexColor);
            agentList.add(agentMap);
        }

        // Ensure "saves/" folder exists
        File saveDir = new File("saves");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        File saveAgentDir = new File("saves/agents");
        if (!saveAgentDir.exists()) {
            saveAgentDir.mkdirs();
        }

        // Build JSON
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < agentList.size(); i++) {
            Map<String, Object> agent = agentList.get(i);
            sb.append("  {\n");
            int count = 0;
            for (Map.Entry<String, Object> entry : agent.entrySet()) {
                sb.append("    \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
                count++;
                sb.append(count < agent.size() ? ",\n" : "\n");
            }
            sb.append(i < agentList.size() - 1 ? "  },\n" : "  }\n");
        }
        sb.append("]\n");

        // Save to file inside the folder
        try (FileWriter writer = new FileWriter("saves/agents/" + filename)) {
            writer.write(sb.toString());
            System.out.println("✅ Saved to saves/" + filename);
        } catch (IOException e) {
            System.err.println("❌ Failed to write file: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> parseJson(String jsonString) {
        List<Map<String, Object>> agentList = new ArrayList<>();

        jsonString = jsonString.trim();
        if (jsonString.startsWith("[")) jsonString = jsonString.substring(1);
        if (jsonString.endsWith("]")) jsonString = jsonString.substring(0, jsonString.length() - 1);

        // Split by JSON objects
        String[] objects = jsonString.split("\\},\\s*\\{");
        for (String obj : objects) {
            obj = obj.trim();
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";

            Map<String, Object> map = new HashMap<>();
            obj = obj.substring(1, obj.length() - 1); // remove braces
            String[] lines = obj.split(",(?=\\s*\"|\\s*[a-zA-Z0-9_]+\\s*:)");

            for (String line : lines) {
                String[] parts = line.trim().split(":", 2);
                if (parts.length == 2) {
                    String key = parts[0].replaceAll("\"", "").trim();
                    String value = parts[1].replaceAll("\"", "").trim();
                    map.put(key, value);
                }
            }

            agentList.add(map);
        }

        return agentList;
    }

    private void loadSettings(String filename) {
        // 🧹 Step 1: Clear existing data
        agent_set.clear();
        agentLookup.clear();
        faceLookup.clear();
        aCount = new Random().nextInt(4);  // Reset face index

        // 🧹 Step 2: Remove all components from the bottomPanel (agent buttons)
        bottomPanel.removeAll();
        bottomPanel.revalidate();
        bottomPanel.repaint();

        // 🧪 Step 3: Load the file
        try (BufferedReader reader = new BufferedReader(new FileReader("saves/agents/" + filename))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String jsonString = sb.toString();
            List<Map<String, Object>> agentList = parseJson(jsonString);
            for (Map<String, Object> agent : agentList) {
                String file = (String) agent.get("filename");
                String locname = (String) agent.get("locname");
                String lang = (String) agent.get("lang");
                String colorHex = (String) agent.get("color");
                Color color = Color.decode(colorHex);
                addAgent(file, locname, lang, color);
                addAgentButton(file, locname, color);
            }
            System.out.println("✅ Loaded " + filename);
        } catch (IOException e) {
            System.err.println("❌ Failed to read " + filename + ": " + e.getMessage());
        }
    }

    class DisplayPanel extends JPanel {
        private Image gifImage;
        private Image shrunkFace;
        private Image smallFace;
        //private Image midRedFace;
        private Image mildFace;
        private Image redFace;
        private Image[] redFaces;
        private Image[] blueFaces;
        private Image[] greenFaces;
        private Image[] yellowFaces;
        private Image[] otherFaces;
        private HashMap<Integer, Image[]> whichFace;

        private Image clockwiseArrow;
        private Image counterclockwiseArrow;
    
        // Constructor or initialization block to load the GIF
        public DisplayPanel() {
            redFaces = new Image[3];
            blueFaces = new Image[3];
            greenFaces = new Image[3];
            yellowFaces = new Image[3];
            otherFaces = new Image[3];
            whichFace = new HashMap<>();
            redFace = new ImageIcon("src/cartoonFace.gif").getImage(); // Load your GIF image here
            mildFace = new ImageIcon("src/mildFace.gif").getImage();
            redFaces[0] = new ImageIcon("src/cartoonFace.gif").getImage();
            redFaces[1] = redFaces[0].getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            redFaces[2] = redFaces[0].getScaledInstance(24, 24,  java.awt.Image.SCALE_SMOOTH);
            blueFaces[0] = new ImageIcon("src/angryEyebrowFace.gif").getImage();
            blueFaces[1] = blueFaces[0].getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            blueFaces[2] = blueFaces[0].getScaledInstance(24, 24,  java.awt.Image.SCALE_SMOOTH);
            greenFaces[0] = new ImageIcon("src/angryTongueFace.gif").getImage();
            greenFaces[1] = greenFaces[0].getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            greenFaces[2] = greenFaces[0].getScaledInstance(24, 24,  java.awt.Image.SCALE_SMOOTH);
            yellowFaces[0] = new ImageIcon("src/dullFace.gif").getImage();
            yellowFaces[1] = yellowFaces[0].getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            yellowFaces[2] = yellowFaces[0].getScaledInstance(24, 24,  java.awt.Image.SCALE_SMOOTH);
            otherFaces[0] = new ImageIcon("src/crosseyedFace.gif").getImage();
            otherFaces[1] = otherFaces[0].getScaledInstance(27, 27,  java.awt.Image.SCALE_SMOOTH);
            otherFaces[2] = otherFaces[0].getScaledInstance(24, 24,  java.awt.Image.SCALE_SMOOTH);
            shrunkFace = mildFace.getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH); 
            smallFace = mildFace.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);
            whichFace.put(0,redFaces);
            whichFace.put(1,blueFaces);
            whichFace.put(2,greenFaces);
            whichFace.put(3,yellowFaces);
            whichFace.put(4,otherFaces); //add different images later
            //resolve direction arrows
            clockwiseArrow = new ImageIcon("src/redclockwise.gif").getImage();
            counterclockwiseArrow = new ImageIcon("src/bluecounterclockwise.gif").getImage();
        }

        public Image whichFaceGet(int fint) {
            return whichFace.get(fint)[0];
        }

        private Image getFace(int myCount, Color myColor) {
            if (myCount < 100) {
                return smallFace;
            }
            int scaler = 0;
            int fint = faceLookup.get(myColor);
            Image[] faces = whichFace.get(fint);
            if (myCount < 201) {
                scaler = 2;
            } else if (myCount < 351) {
                scaler = 1;
            }
            //System.out.println("FACE STUFF fint: " + fint + " scaler: " + scaler);
            //System.out.println("Count: " + myCount + " Color: " + myColor.getRed() + ", " + myColor.getGreen() + ", " + myColor.getBlue());
            return faces[scaler];
        }
        private HashMap<Integer,Integer> findCenterIndicesAndSums(Simulation.World_State dState, int minSize) {
            ArrayList<Integer> centerIndices = new ArrayList<>();
            ArrayList<Integer> sums = new ArrayList<>();
            HashMap<Integer,Integer> indexToSum = new HashMap<>();
            
            int n = dState.owners.size();
            int start = 0;
    
            while (start < n) {
                int end = start;
                
                // Find the end of the sublist with the same owner
                while (end < n && dState.owners.get(end).equals(dState.owners.get(start))) {
                    end++;
                }
    
                int sublistSize = end - start;
                if (sublistSize >= minSize) {
                    // Calculate the center index
                    int centerIndex = start + (sublistSize - 1) / 2;
                    //centerIndices.add(centerIndex);
    
                    // Calculate the sum of the counts in this sublist
                    int sum = 0;
                    for (int i = start; i < end; i++) {
                        sum += dState.counts.get(i);
                    }
                    //sums.add(sum);
                    if (dState.owners.get(start) != Color.GRAY) {
                        indexToSum.put(centerIndex,sum);
                    }  
                }
    
                // Move to the next group
                start = end;
            }
    
            // Print the results
            //System.out.println("Center indices of sublists: " + centerIndices);
            //System.out.println("Sums of counts for those sublists: " + sums);
    
            return indexToSum;
        }
    
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
    
            if (step == 0) {
                Font f = new Font("Arial", Font.BOLD, 15);
                g.setFont(f);
                g.setColor(Color.BLACK);
                g.drawString("PRESS RELOAD BUTTON", 50, 50);
                return;
            }
    
            String stepS = Integer.toString(step);
            String stepD = Integer.toString(displayStep);
            g.setColor(Color.BLACK);
            g.drawString("Last Game Step: " + stepS, 40, 33);
            g.drawString("Current Display Step: " + stepD, 40, 43);
            //g.drawImage(gifImage, 50, 75, this);
    
            ArrayList<Simulation.World_State> state_history = sim.get_state_history();
            System.out.println(displayStep);
            Simulation.World_State drawState = state_history.get(displayStep-1);
            //System.out.println(drawState);
            sim.world.printNodes();
    
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(getWidth(), getHeight()) / 2 - 20; // Radius of the outer circle
            int arcAngle = 360000 / drawState.counts.size(); // Each segment has equal size
    
            int startAngle = 0; // Starting angle for the first arc
            //Map counts and owners to find the indexes of nodes which are surrounded by similar color nodes.
            //Then when the size is large you can draw faces at those indexes.
            for (int i = 0; i < drawState.counts.size(); i++) {
                int count = drawState.counts.get(i);
                Color c = drawState.owners.get(i);
    
                // Fill the arc with the specified color
                g.setColor(c);
                g.fillArc(centerX - radius, centerY - radius, 2 * radius, 2 * radius, startAngle/1000, (1000+arcAngle)/1000);
    
                // Calculate the midpoint angle of the arc to place the text
                int midAngle = startAngle + arcAngle / 2;
                double angleRad = Math.toRadians(midAngle/1000);
                int textRadius = radius + 10; // Position text slightly outside the arc
    
                // Adjust text position based on the angle
                int textX = centerX + (int) (textRadius * Math.cos(angleRad)) - g.getFontMetrics().stringWidth(String.valueOf(count)) / 2;
                int textY = centerY - (int) (textRadius * Math.sin(angleRad)) + g.getFontMetrics().getHeight() / 4;
                int text2X = centerX + (int) ((textRadius-25) * Math.cos(angleRad)) - g.getFontMetrics().stringWidth(String.valueOf(i)) / 2;
                int text2Y = centerY - (int) ((textRadius-25) * Math.sin(angleRad)) + g.getFontMetrics().getHeight() / 4;
    
                // Draw the count value near the arc
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(count), textX, textY);
                if (debug_mode) {
                    g.drawString(String.valueOf(i), text2X, text2Y);
                }
                
                // If count > 0, draw the GIF in the center of the arc
                if ((!debug_mode) & (count > 0) & (wSize < 41) & (c != Color.GRAY)) {
                    Image myFace = getFace(count, c);
                    int gifX = centerX + (int) (radius * 0.9 * Math.cos(angleRad)) - myFace.getWidth(this) / 2;
                    int gifY = centerY - (int) (radius * 0.9 * Math.sin(angleRad)) - myFace.getHeight(this) / 2;
                    g.drawImage(myFace, gifX, gifY, this);
                    
                }
    
                // Update start angle for the next arc
                startAngle += arcAngle;
            }
    
            // Draw the white inner circle last, smaller than the outer circle
            int innerRadius = (int) (radius * 0.8); // 80% of the outer circle's radius
            g.setColor(Color.WHITE);
            g.fillOval(centerX - innerRadius, centerY - innerRadius, 2 * innerRadius, 2 * innerRadius);

            //Draw the arrows for resolve direction
            int locangle = drawState.resolve_start*arcAngle;
            double locangleRad = Math.toRadians(locangle/1000);
            int ngifX = centerX + (int) (radius * 0.7 * Math.cos(locangleRad)) - counterclockwiseArrow.getWidth(this) / 2;
            int ngifY = centerY - (int) (radius * 0.7 * Math.sin(locangleRad)) - counterclockwiseArrow.getHeight(this) / 2;
            if (drawState.resolve_dir == 1 & drawState.step != 0) {
                g.drawImage(clockwiseArrow, ngifX, ngifY, this);
            }
            if (drawState.resolve_dir == 0 & drawState.step != 0) {
                g.drawImage(counterclockwiseArrow, ngifX, ngifY, this);
            }

            if (drawState.victory & !singleMode) {
                String victor = "N";
                for (String s : drawState.player_totals.keySet()) {
                    if (drawState.player_totals.get(s) > 0) {
                        victor = s;
                    }
                }
                Color vicColor = agentLookup.get(victor).getColor();
                g.setColor(vicColor);
                g.fillOval(centerX - innerRadius, centerY - innerRadius, 2 * innerRadius, 2 * innerRadius);
                Image vicFace = getFace(1000, vicColor);
                //Image vicFace = vicFace1.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH);
                g.drawImage(vicFace, centerX, centerY, this);
                g.setColor(Color.BLACK);
                g.drawString("Victor is : " + victor, 50, 250);
                return;
            }

            if ((!debug_mode) & (wSize > 40)) {
                int minNodes = 1 + wSize / 40;
                HashMap<Integer,Integer> locCount = findCenterIndicesAndSums(drawState,minNodes);
                for (int x : locCount.keySet()) {
                    Color c = drawState.owners.get(x);
                    int count = locCount.get(x);
                    Image myFace = getFace(count, c);
                    int myAngle = (x+1)*arcAngle/1000;
                    double myAngleRad = Math.toRadians(myAngle);
                    int gifX = centerX + (int) (radius * 0.9 * Math.cos(myAngleRad)) - myFace.getWidth(this) / 2;
                    int gifY = centerY - (int) (radius * 0.9 * Math.sin(myAngleRad)) - myFace.getHeight(this) / 2;
                    g.drawImage(myFace, gifX, gifY, this);
                }
                
            }
            if (debug_mode) {
                //System.out.println("DRAWSTATE SIZE: " + String.valueOf(drawState.moves.size()));
                g.setColor(Color.RED);
                g.drawString("   :::MOVEMENTS:::", 10, 60);
                if (drawState.moves.isEmpty()) {
                    return;
                }
                int yy = 0;
                for (Simulation.Movement move: drawState.moves) {
                    g.setColor(Color.BLACK);
                    //System.out.println("DRAWSTATE Should print: " + move.change + String.valueOf(drawState.moves.size()));
                    //int aStart = sim.world.get_global_perspective(0,move.agent.locname);
                    int aStart = move.agent.myStart;
                    g.drawString("(" + String.valueOf(yy) + ") " + move.agent.locname + " places " + String.valueOf(move.change) + " at " + String.valueOf(move.loc) + " (+" + String.valueOf(aStart) + ")", 10, 70+(yy*10));
                    yy++;
                }
                for (Agent_Details ag : sim.agents) {
                    int tot = drawState.get_player_total(ag);
                    g.drawString(">>> " + ag.locname + " has " + tot + " fernies", 10, 70+(yy*10));
                    yy++;
                }
                if (drawState.resolve_dir == 1) {
                    g.drawString("BATTLES COUNTERCLOCKWISE", 10, 70+(yy*10));
                    yy++;
                    g.drawString("FROM NODE: " + (int) wSize/4, 10, 70+(yy*10));
                    yy++;
                }
                if (drawState.resolve_dir == 0) {
                    g.drawString("BATTLES CLOCKWISE", 10, 70+(yy*10));
                    yy++;
                    g.drawString("FROM NODE: " + (int) 3*wSize/4, 10, 70+(yy*10));
                    yy++;
                }
                
            }
        }
    }
    
    private void updateSimulation() {        
        //needs to check if display step < step
        //this.step++;
        sim.make_turn();
        this.step = sim.step;
        this.displayStep++; // = this.step;
        //this.displayStep = this.step;
        this.displaySuperStep = sim.step;
        //this.superStep2Step.put(displaySuperStep,displayStep);
        //System.out.println(this.step);
        simdisplayPanel.repaint(); // Repaint the display panel to show the new state
    }
    
    private void updateSimulationNoDisplay() {        
        //needs to check if display step < step
        this.step++;
        sim.make_turn();
        this.displayStep = this.step;
    }

    private void loadAgentsFromFile(String line) {
        String[] parts = line.split(",");
        String name = parts[0];
        String loc = parts[1];
        String lang = parts[2];
        Color myColor = new Color(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]));
        addAgent(name, loc, lang, myColor);
        addAgentButton(name, loc, myColor);
    }

private void styleSettingButton(JButton button) {
    // Base styles
    button.setFocusPainted(false);
    button.setContentAreaFilled(true);
    button.setOpaque(true);
    button.setPreferredSize(new Dimension(180, 80));
    button.setFont(new Font("SansSerif", Font.BOLD, 14));
    button.setHorizontalAlignment(SwingConstants.CENTER);
    button.setVerticalAlignment(SwingConstants.CENTER);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    // Light background
    button.setBackground(new Color(245, 245, 245)); // Light gray

    // 3D shadow-style border
    Border outer = BorderFactory.createLineBorder(new Color(180, 180, 180), 2, true); // Outer soft border
    Border inner = BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.WHITE, Color.LIGHT_GRAY); // 3D effect
    button.setBorder(BorderFactory.createCompoundBorder(outer, inner));

    // Optional: subtle mouse hover effect
    button.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            button.setBackground(new Color(230, 230, 230)); // Lighter gray
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            button.setBackground(new Color(245, 245, 245)); // Original
        }
    });
}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            COREapp app = new COREapp();
            app.setVisible(true);
            if (args.length > 0) {
                try (BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        app.loadAgentsFromFile(line);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading the settings file: " + e.getMessage());
                }
            }
        });
    }
}
