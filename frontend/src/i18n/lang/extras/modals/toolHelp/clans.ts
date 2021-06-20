/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            clans: {
                overview: `
                <h5><b>Overview</b> <a href="/clans/clans_userguide.pdf" target="”_blank”">(PDF version)</a></h5>
                <p>
                    CLANS is a Java program for visualizing the relationship between proteins based on their allagainst-all
                    pairwise sequence similarities. The program implements a version of the Fruchterman-Reingold force 
                    directed graph layout algorithm to present the sequence similarities in a 2D or 3D graph.
                </p>
                <p>
                    CLANS can get as an input a set of sequences in FASTA format, perform an all-against-all BLAST search to
                    obtain a matrix of sequence similarities and display it as a dynamic graph using the Fruchterman-Reingold
                    layout. Alternatively, a matrix with precomputed "attraction" values can be provided.
                </p>
                <p>
                    The E-values of the BLAST HSPs are used to calculate the attractive forces between each sequence pair.
                    The lower (better) the E-value, the higher the attractive force. In addition, each sequence repulses
                    every other sequence with a certain force (inversely proportional to their distance in space). Clustering
                    is achieved by iteratively moving sequences according to the force vector resulting from all pairwise
                    interactions (attraction and repulsion).
                </p>
                <h5><b>Prerequisites for running CLANS</b></h5>
                <p>
                    Java Runtime Environment (JRE) should be installed on the computer.
                </p>
                <h5><b>Input file</b></h5>
                <p>
                    A file in special ‘CLANS’ format (.clans file) that was created either by the CLANS web-utility
                    in the MPI Bioinformatics Toolkit (for the initial run) or by a previous session of the CLANS
                    tool (CLANS saved-file).
                </p>
                    The ‘CLANS’ file format must contain the following blocks of information:
                <ul>
                    <li>The first line must be: <span style="color:#2E8C81;font-style: italic">sequences=&lt;number of sequences&gt;</span></li>
                    <li>The sequences block: the original sequences in FASTA format (the order of the
                        sequences is important and is further used to index the sequences, starting from 0).
                        <div style="color:#2E8C81;font-style: italic">
                            &lt;seq&gt;<br>
                            >seq0<br>
                            MSGRGKQGGKARAKAKTRSSRAGLQFPVGR<br>
                            >seq1<br>
                            LAAEVLELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLSGVT<br>
                            &lt;/seq&gt;<br>
                        </div></li>
                    <li>The coordinates block: the positions of the sequences in the 3D space.
                        Every line contains the sequence index and a value for the X, Y, Z coordinates (0&lt;X,Y,Z&lt;1).
                        <div style="color:#2E8C81;font-style: italic">
                            &lt;pos&gt;<br>
                            0 0.142 0.281 0.104<br>
                            1 0.298 0.631 0.913<br>
                            &lt;/pos&gt;<br></div></li>
                    <li>The BLAST HSPs block: the E-values for the pairwise sequence similarities.
                        <div style="color:#2E8C81;font-style: italic">
                            &lt;hsp&gt;<br>
                            0 1:5.1e-05<br>
                            0 4:1.1e-02<br>
                            0 5:6.8e-04<br>
                            &lt;/hsp&gt;<br>
                    </div></li>
                </ul>
                The file may contain additional blocks:
                <ul>
                    <li>Parameters block: a list of all the parameters that were used in the calculation and presentation 
                        of the saved session.
                        <div style="color:#2E8C81;font-style: italic">
                            &lt;param&gt;<br>
                            maxmove=0.1<br>
                            pval=1.0<br>
                            &lt;param&gt;<br>
                        </div></li>
                    <li>The rotation matrix block: the rotation matrix for the current clustering.
                        <div style="color:#2E8C81;font-style: italic">
                            &lt;rotmtx&gt;<br>
                            1.0;0.0;0.0;<br>
                            0.0;1.0;0.0;<br>
                            0.0;0.0;1.0;<br>
                            &lt;/rotmtx&gt;<br>
                        </div></li>
                </ul>
                <h5><b>BLAST search</b></h5>
                The stage of all-against-all BLAST search to obtain the matrix of pairwise sequence
                similarities in ‘CLANS’ special format (.clans file) can be done using the CLANS web-utility in
                the MPI Bioinformatics Toolkit. It accepts sequences in FASTA format (up to 10,000
                sequences) and produces a file in CLANS format that can be later loaded and visualized in
                the CLANS tool. The scoring matrix and the E-value threshold for the BLAST search can be
                set by this utility.<br><br>
                <h5><b>Opening the CLANS graphical user interface (GUI)</b></h5>
                The CLANS executable (clans.jar) is a Java JAR file. It can be executed in two ways:
                <ol>
                    <li>From the command-line.</li>
                    <li>By launching the GUI and loading a pre-calculated ‘CLANS’ or matrix file.</li>
                </ol>
                <b>1. Executing CLANS from the command line</b><br>
                The command to execute CLANS from the command line is:<br>
                <div style="color:#2E8C81;font-style: italic">java [-Xmx4G] -jar clans.jar [-load &lt;clans file&gt;]</div>
                Note: clans.jar must have executing permissions (chmod +x clans.jar will grant such permissions). <br><br>
                Optional parameters:
                <ul>
                    <li>
                        <span style="color:#2E8C81;font-style: italic">-load &lt;path of clans file&gt;</span>: Opens the CLANS
                        GUI and loads the sequences information into it using a ‘CLANS’ formatted file. When -load is omitted,
                        the CLANS GUI is started empty and a CLANS input-file can then be loaded using the ‘Load Run’ menu item.
                    </li>
                    <li>
                        <span style="color:#2E8C81;font-style: italic">-Xmx&lt;number&gt;m / -Xmx&lt;number&gt;G</span>: An optional Java
                        parameter, which specifies the maximum memory allocation pool (in Mb or in Gb) for the Java virtual machine
                        (JVM) and can be omitted or increased if needed. This parameter may be useful when loading very large
                        files (&gt;50Mb). It can then be set to -Xmx8G and even more (depending on the memory capabilities of the computer).
                    </li>
                </ul>
                <b>2. Launching the GUI directly from the file browser</b><br>
                When Java Runtime Environment is installed on the computer, double-clicking the clans.jar
                executable opens CLANS graphical interface (without a pre-loaded file). Please note that
                when launching the program in this way, it is not possible to set the memory allocation.<br><br>

                <h5><b>Clustering the sequences using CLANS graphical interface</b></h5>
                If the GUI was opened without a file, the first stage is to load a file in ‘CLANS’ format:<br>
                <span style="color:#2E8C81;font-style: italic">Menu -&gt; File -&gt; Load Run.</span><br>
                <ul>
                    <li>
                        <b>1. Loading a ‘CLANS’ file that was created by the CLANS web-utility in the MPI Bioinformatics Toolkit:</b>
                        In this case, this is the initial visualization of the sequences. The sequences are
                        presented as dots in the 3D space and their positions are randomly determined. The
                        clustering process will start by clicking the <span style="color:#2E8C81;">Start run</span> button.
                    </li>
                    <li>
                        <b>2. Loading a saved-file that was created by a previous session of CLANS:</b>
                        In this case, the sequences are visualized exactly as they were saved in the previous
                        session. The clustering process may be continued from the same point by clicking the
                        <span style="color:#2E8C81;">Start run / Resume</span> button.
                    </li>
                </ul>
                
                <b>Button options:</b><br>
                <ul>
                    <li>
                        <b>Initialize</b>: randomize the sequence positions in space prior to the clustering process.
                    </li>
                    <li>
                        <b>Start run / Stop / Resume</b>: start a new clustering process (at the 
                        first time or after initializing) / Stop an existing clustering process at a certain iteration / Resume the
                        clustering from the same point.
                    </li>
                    <li>
                        <b>Select / Move</b>: by default, the Move option is toggled and enables to rotate the 3D-
                        space using the mouse.<br>
                        When the Select option is toggled (the button is colored in blue), it is possible to
                        select specific sequences by marking a specific area on the 3D graph using the
                        mouse. The selected sequences are then marked in red and different operations can
                        be performed on them.
                    </li>
                    <li>
                        <b>Show selected</b>: display the names of the selected sequences in a different window. By
                        default, displays all the sequences.
                    </li>
                    <li>
                        <b>Select All / Clear Selection</b>: toggle between selecting all the sequences and clearing
                        the current selection.
                    </li>
                    <li>
                        <b>Zoom on selected / Show all</b>: enable to zoom in to the selected sequences / Zoom out
                        again to display all the sequences.
                    </li>
                    <li>
                        <b>Use P-values better than &lt;P-value threshold&gt;</b>: consider only sequence pairs with E-
                        values better (=lower) than specified (in the next text field) as connected. Changing
                        the P-value threshold may influence the clustering (it affects the calculation of the
                        attractive forces) as well as the connections display (when checking the ‘show
                        connections’ option). The P-value can be given as an integer, a float number or in
                        exponential notation (for example: 2.0e-10). Please note that after changing the P-
                        value threshold you must hit the ‘return’ key to submit the change to the program.<br>
                        *The text field right to the P-value threshold (cannot be changed) presents the
                        highest (=worst) E-value among the BLAST HSPs.
                    </li>
                </ul>
                <b>Checkboxes:</b><br>
                <ul>
                    <li>
                        <b>show names</b>: display the sequence names on the graph.
                    </li>
                    <li>
                        <b>show numbers</b>: display the sequence indices on the graph.
                    </li>
                    <li>
                        <b>show connections</b>: display the edges connecting the sequences in the graph. The color
                        of the edge reflects the pairwise sequence similarity (darker grey for higher similarity
                        and lighter for lower similarity) and can be set via Menu -> Draw -> Change color (dot
                        connections). Please consider that displaying the connections during the clustering
                        process slows slows the graphical visualization, especially when there is a large number of
                        sequences.
                    </li>
                </ul>                
                <b>Selected Sequences window (Show selected)</b>:
                When selecting sequences and pressing the Show selected button, a window is opened and
                presents the names of the selected sequences (by their original order). At the bottom of the window there
                are buttons enabling to perform different operations on the list of sequences. Please note that when
                pressing the Show selected button when no sequence is selected, the window presents all the sequences
                in the CLANS file.
                <ul>
                    <li>
                        <b>Find</b>: open a search window, in which a text (exact or not) can be entered and
                        searched among the sequences. When clicking the OK button (note that the Return
                        key will not execute the operation), the sequences in which the search term is found
                        are then marked in blue. Clicking the OK button again completes the operation and
                        presents only the marked sequences in the Selected Sequences window (here too,
                        only the OK button executes the operation and not the Return key). The operation
                        can be cancelled by clicking the Back button.
                    </li>
                    <li>
                        <b>Show all names / show selected names</b>: toggle between showing only the selected
                        sequences and showing the names of all the sequences in the CLANS file.
                    </li>
                    <li>
                        <b>Save to file</b>: save to file the current displayed sequences (selected / all / sequences
                        containing a certain search term).
                    </li>
                    <li>
                        <b>Back</b>: go one operation back within the Selected Sequences window.
                    </li>
                    <li>
                        <b>Clear</b>: clear the last operations memory and retain only the last selection. After Clear it
                        is not possible to display the initial selection again.
                    </li>
                    <li>
                        <b>Close</b>: close the Selected Sequences window and retain the last selection made.
                    </li>
                </ul>
                *Please note that defining a new selection in the Selected Sequences window (using Find),
                affects the selections in the main GUI window and changes it accordingly.<br><br>
             
                <b>Menu options</b><br>
                <b>>File</b><br>
                <b>>>Commonly-used options:</b><br>
                <ul>
                    <li>
                        <b>Load Run:</b> open a CLANS file (that was created by either the MPI Toolkit web-utility or
                        saved by a previous run of CLANS) and displays the sequences as dots in the 3D-
                        space according to the parameters and positions saved in the CLANS file.
                    </li>
                    <li>
                        <b>Save Run</b>: save the current display (including the positions of the sequences and other
                        defined parameters) in a ‘CLANS’ formatted file.
                    </li>
                </ul>
                <b>>>Advanced options</b>:<br>
                <ul>
                    <li>
                        <b>Save attraction values to file</b>: save a list of the pairwise attraction values that meet the
                        P-value threshold, to a file in the following format:<br>
                        <em>seq1_index seq2_index attraction_value</em><br>
                        (Attraction value = -log(E-value), divided by the highest value).
                    </li>
                    <li>
                        <b>Save 2d graph data</b>: save the sequences names together with their (X,Y) coordinates
                        in the following format:<br>
                        <em>seq_index seq_name X Y</em>
                    </li>
                    <li>
                        <b>Print view</b>: print or saves the current graphical presentation in PDF format.
                    </li>
                </ul>
                
                <b>>Misc</b><br>
                <b>>>Commonly-used options</b>:</b><br>
                <ul>
                    <li>
                        <b>Extract selected sequences</b>: save to file the currently selected sequences in FASTA format.
                    </li>
                    <li>
                        <b>Hide singletons</b>: remove sequences that are not connected to any other sequence from the graph.
                    </li>
                    <li>
                        <b>Cluster in 2D</b>: when this option is checked, the clustering is performed in two dimensions instead
                        of three. Clustering in 2D is recommended when generating figures out of CLANS graph.
                    </li>
                </ul>
                <b>>>Advanced options:</b>
                <ul>
                    <li>
                        <b>Use selected subset</b>: display only the selected sequences (similar to Zoom-In).
                    </li>
                    <li>
                        <b>Use parent group</b>: undo the last ‘Use selected subset’ operation.
                    </li>
                    <li>
                        <b>Set rotation values</b>: set the values for the current rotation matrix (9 values, separated by commas).
                    </li>
                    <li>
                        <b>Rescale attraction values</b>: when this option is checked, the attraction values are normalized 
                        according to the current P-value threshold, where 0 is the current lowest attraction value and 1 is 
                        the highest attraction value. Please note that rescaling lowers the attraction values and thus makes 
                        the clusters less condensed.<br>
                        (After checking/unchecking this option, put the cursor on the P-value threshold text- field and hit the
                        ‘Return’ key, for the change to take action).
                    </li>
                    <li>
                        <b>Only draw every Nth round</b>: set a new value (instead of 1) for the iterations interval for drawing 
                        the new positions of the dots in the graph. This makes the drawing less smooth but speeds up the 
                        clustering process.
                    </li>
                </ul>
                
                <b>>Draw</b><br>
                <b>>>Commonly-used</b>
                <ul>
                    <li>
                        <b>Set dot size</b>: set the size of the dots representing the sequences in the graph (the default and minimum is 2).
                    </li>
                    <li>
                        <b>Set selected circle size</b>: set the size of the circle highlighting the selected sequences (dots) 
                        and the sequence groups.
                    </li>
                    <li>
                        <b>Center graph</b>: set the current view on the center of the graph.
                    </li>
                    <li>
                        <b>Antialiasing</b>: when this option is checked, spatial anti-aliasing is enabled (the graphics is
                        nicer but slower, especially if the connecting lines are displayed). It is recommended to turn-on
                        the anti-aliasing in the end of the clustering process, for the purpose of generating an image of 
                        CLANS map (it smooths the lines and shapes of the graph).
                    </li>
                    <li>
                        <b>Stereo</b>: when this option is checked, a stereo image is displayed. The stereo image is composed 
                        of two identical 2D graphs, with a small angle between them, that are viewed separately by the left and 
                        right eyes of the viewer, to give the perception of 3D depth.
                    </li>
                    <li>
                        <b>Change stereo angle </b>:</NOBR> change the angle between the left and right views of the stereo image 
                        (the default angle is 4).
                    </li>
                </ul>
                <b>>>Advanced options:</b>
                <ul>
                    <li>
                        <b>Change Font</b>: set the font used in the graph display (affects the text that appears inside the
                        graph area, for example: the sequences names).
                    </li>
                    <li>
                        <b>Change color (dot connections)</b>: open a window, where the colors of the edges (dot connections) 
                        can be set according to their P-values (by default, the color gradient is from light grey for the worst 
                        P-values to black for the best P-values).
                        <ul>
                            <li>
                                <b>Set the P-value thresholds for the bins</b>: it is possible to set a value for each bin separately or 
                                define the minimal and maximal values and use the ‘Value gradient’ button to equally distribute the 
                                values between the bins.
                            </li>
                            <li>
                                <b>Set the colors of the bins</b>: it is possible to set a color for each bin separately or define the 
                                colors of the minimal and maximal values and use the ‘Color gradient’ button to create a gradient 
                                from these colors.
                            </li>
                        </ul>
                    </li>
                    <li>
                        <b>Change color (Foreground)</b>: set the foreground color of the text displayed in the graph area (the default is black).
                    </li>
                    <li>
                        <b>Change color (Background): </b>set the color for the background of the graph area.
                    </li>
                    <li>
                        <b>Change color (Selected)</b>: set the color of the ovals highlighting the selected sequences.
                    </li>
                    <li>
                        <b>Change color (BLAST hits numbers)</b>: change the color of the HSP sequence numbers, presented when 
                        choosing Window -&gt Show BLAST hits for sequence and the option Draw -&gt; Show hsp sequence numbers is checked.
                    </li>
                    <li>
                        <b>Change color (BLAST hits circles)</b>: change the color of the circles highlighting the sequences 
                        having BLAST hits for a selected sequence (when choosing Window -&gt; Show BLAST hits for sequence.
                    </li>
                    <li>
                        <b>Color dots by sequence length</b>: when this option is checked, the dots representing the sequences are
                        colored according to their length (yellow=shortest, blue=longest, gradient=in-between). (After 
                        checking/unchecking this option, put the cursor on the graph area and left-click the mouse for the change
                        to take action).
                    </li>
                    <li>
                        <b>Color by edge "frustration”</b>: when this option is checked, the edges in the graph are colored 
                        according to whether they are longer(red) or shorter(blue) than they should be according to the 
                        attraction values in the matrix.
                    </li>
                    <li>
                        <b>Show origin</b>: when this option is checked, the origin (0,0,0) is marked in red on the graph area. 
                        (After checking/unchecking this option, put the cursor on the graph area and left-click the mouse for 
                        the change to take action).
                    </li>
                    <li>
                        <b>Show info</b>: when this option is checked (it is checked by default), information about the current 
                        clustering is displayed (the edges coloring, maximum X,Y coordinates, current rotation matrix).
                    </li>
                    <li>
                        <b>Show HSP sequence numbers</b>: this option is related to the ‘Show blast hits for sequence’ option 
                        (from the ‘Windows’ menu item). When it is checked, and the BLAST hits for a certain sequence are 
                        highlighted, the hits sequence numbers are also presented on the graph (next to the points representing them).
                    </li>
                    <li>
                        <b>Zoom</b>: set a zoom factor for the view (default: 100%; fits all vertices to the screen).
                    </li>
                </ul><br>
            
                <b>Windows</b><br>
                <b>>>Commonly-used options:</b><br>
                <ul>
                    <li>
                        <b>Show options window</b>: open a pop-up window which enables to set several parameters related to the 
                        clustering algorithm. For any change of parameter to take action, clicking the ‘return’ key or the ‘maxmove’ 
                        button is needed.
                        <ul>
                            <li>
                                <b>Cooling</b>: a multiplier for the ‘maxmove’ parameter (see below), can be set between 0 and 1. 
                                When cooling=1 (the default), maxmove does not converge to 0 and the dots keep moving infinitely. 
                                When cooling &lt; 1, maxmove converges to 0 and the graph reaches a state where the dots stop moving at all 
                                (the rate of this convergence depends on the value of the cooling parameter. The closer it is to 1, the 
                                slower the convergence = more iterations are needed to reach convergence).
                            </li>
                            <li>
                                <b>Current cooling</b>: display (cannot be set) the changing “temperature” of the system during the 
                                clustering process. When the cooling parameter is set to 1, the current cooling (temperature) remains 1 
                                as well and does not change. But when the cooling parameter &lt; 1, the system “cools down” during the 
                                clustering process until it reaches convergence.
                            </li>
                            <li>
                                <b>Maxmove</b>: the maximum distance a point is allowed to move per round (the default is 0.1).<br>
                                It makes sense to increase the maxmove parameter when decreasing the cooling to allow bigger movements 
                                of the points in each round, since the number of rounds is limited.
                            </li>
                            <li>
                                <b>Attract value</b>: a multiplier factor for the calculation of the attractive force between each two 
                                sequences (default=10).
                            </li>
                            <li>
                                <b>Attract exponent (int)</b>: determine how the attractive force scales with the distance between each 
                                two vertices in the graph (default=1, attraction increases linearly with the distance).
                            </li>
                            <li>
                                <b>Repulse value</b>: a multiplier factor for the calculation of the repulsive force between each two 
                                sequences (default=10).
                            </li>
                            <li>
                                <b>Repulse exponent</b>: determine how the repulsive force scales with the distance between each two 
                                vertices in the graph (default=1, repulsion decreases linearly with the distance).
                            </li>
                            <li>
                                <b>Dampening</b>: a value between 0 and 1, determines to what extent the movement vector of the last 
                                movement affects the current movement (default=0.2). The higher the dampening parameter, the higher the 
                                previous movement influence. When it is set to 0, there is no influence.
                            </li>
                            <li>
                                <b>Min. attraction</b>: a minimal force that attracts each sequence towards the origin of the graph 
                                (also called “gravity”, default=1). This gravity force keeps unconnected clusters/sequences from drifting 
                                apart indefinitely. It scales linearly with the distance.
                            </li>
                            <li>
                                <b>Cluster for rounds</b>: set the number of iterations to be performed in the clustering process whenever 
                                the Start Run/Resume button is pressed (default=-1). When it is set to -1, the number of rounds is infinite 
                                and will be stopped only by the user.
                            </li>
                        </ul>
                    </li>
                    <li>
                        <b>Selected</b>: open the ‘Selected Sequences’ window (same as using the ‘Show selected’ button), which 
                        displays the names of the selected sequences. By default, it presents the names of all the sequences. If 
                        the selection is changed while the ‘Selected Sequences’ window is already open, there is a need to press 
                        the ‘Show selected’ button again to update the presentation in the window.
                    </li>
                    <li>
                        <b>Edit Groups</b>: The Edit Groups window enables to set/edit different attributes for sequence groups 
                        (pre-defined or selected interactively) and to perform different operations on the groups. When saving 
                        the current run as a ‘CLANS’ file, the groups and their settings are saved in a separate block, 
                        marked by the &lt;seqgroups&gt; tag. <br><br>
                        Sequence groups can be defined or added in several ways:<br>
                        <ul>
                            <li>
                            Manually in the CLANS file, in the following format:
                                <div style="color:#2E8C81;font-style: italic">
                                    &lt;seqgroups&gt;<br>
                                    name=group1<br>
                                    type=1<br>
                                    size=6<br>
                                    hide=0<br>
                                    color=153;0;51;255<br>
                                    numbers=435;436;437;438;439;440;<br>
                                    &lt;/seqgroups&gt;
                                </div>
                            </li>
                            <li>
                            By searching for clusters (Windows -&gt; Find clusters) and defining each cluster as a sequence group 
                            using the Add each as separate sequence group button.
                            </li>
                            <li>
                            From the Edit Groups window, by using the Add selected button.
                            </li>
                        </ul><br>
                The last option is the one commonly used. To form a group in this way, sequences must be selected and 
                converted to a group using the Add selected button. Upon clicking that button, a pop-up window allows to 
                name the group. The group then appears with the given name in the <span style="color:#2E8C81;">Edit Groups</span> window, followed in 
                brackets by the number of sequences in it. In order to visualize the group in the CLANS map, the 
                checkbox Draw groups must be clicked on.<br><br>
                By default, a new group is shown as red colored circles of size 4.<br><br>
                The following attributes can be changed for each group after selecting it using the mouse (blue highlight):<br>
                <ul>
                    <li>
                        <b>Shape (field to the right of the group names)</b>: the circles can be changed to other shapes by 
                        clicking in this field.
                    </li>
                    <li>
                        <b>Size (buttons above and below the Shape field)</b>: the size of the shapes (default=5) can be changed.
                    </li>
                    <li>
                        <b>Hide/Show button</b>: whether this group is displayed in the graph or not.
                    </li>
                    <li> 
                        <b>Change name button</b>: change the name of group.
                    </li>
                    <li>
                        <b>Change color button</b>: change the color of the circles (or other shapes) marking a group.<br>
                        If the Color group names option is checked, the group names will also be colored in the same color.
                    </li>
                </ul><br>

                Other operations that can be done using the buttons:
                <ul>
                    <li>
                        <b>Set as selected</b>: set the sequences belong to the currently selected group as selected in the graph.
                    </li>
                    <li>             
                        <b>Move up / Move down</b>: move the selected group one step up or down in the groups list.
                    </li>
                    <li>
                        <b>Delete</b>: remove the selected group(s) from the groups list.
                    </li>
                </ul>
            </li>
        </ul>
                
        <b>>>Advanced options:</b>
        <ul>
            <li>
                <b>P-value plot:</b> open a window showing the distribution of P-values (or attraction values) for the current dataset.
            </li>
            <li>
                <b>Show blast hits for sequence</b>: first, a window with all the sequences names is opened and it is 
                possible to select one sequence. Then, a window showing the distribution of HSPs throughout 
                the selected sequence is opened. Clicking with the mouse inside this new window first highlights the 
                selected sequence. Moving the red slider throughout the distribution, highlights the related HSPs in 
                the graph (in the color defined by <span style="color:#2E8C81;">Draw -&gt; Change color (BLAST hits circles)</span>). If the option 
                <span style="color:#2E8C81;">Draw -&gt; Show HSP sequence numbers</span> is checked, the HSP sequence numbers are presented in the graph 
                as well (in the color defined by <span style="color:#2E8C81;">Draw -&gt; Change color (BLAST hits numbers)</span>).
            </li>
            <li>
                <b>Find Clusters:</b> determine which clusters exist in the dataset. The clustering can be done by one of three methods:
                <ul>
                    <li>
                        <b>N-linkage clustering (the default method)</b>: a simple clustering according to a minimal number of 
                        connections between the sequences, defined by the user (the default is 1). Two sequences are defined as 
                        connected if their sequence similarity E-value is better than the current P-value threshold. Thus, 
                        changing the P-value threshold parameter may influence the clustering.
                    </li>
                    <li>
                        <b>Convex clustering</b>: all the sequences, which their average sequence-cluster attractive force is 
                        better than X*SD (standard deviation) of the average attraction for the dataset, are grouped together 
                        (X can be set by the user, default is 0.5). This method is much slower than the N-linkage clustering.
                    </li>
                    <li>
                        <b>Network-based clustering</b>: each sequence forms a node of the input layer for a network. These nodes 
                        emit the number of the cluster the sequence belongs to (at the beginning: number of clusters=number of sequences). 
                        The "weight" of each value is proportional to the -log(P-value) of the blast hit. The second 
                        layer integrates all these inputs and emits the cluster number with the highest sum of entries for each 
                        sequence. This value is then fed back as the new "cluster assignment" for the sequence to the input layer. 
                        The above steps are repeated until no cluster assignment changes (generally 5 to 6 rounds).
                    </li>
                </ul>
            It is possible to combine each of the above methods with a <span style="color:#2E8C81;">jackknife test</span>. The user can set the number 
            of replicates to perform (default=100) and the amount of data to disregard in each replicate (default=0.1). 
            Two confidence values are calculated:<br>
                <ul>
                    <li>
                    For each cluster - how often each cluster appears exactly the way it is in the replicates (the values are displayed in the Clusters window).
                    </li>
                    <li>
                    For each sequence - how often each sequence is assigned to the same cluster. The confidence values are 
                    displayed in the graph (black=low- confidence, red=high confidence).
                    </li>
                </ul>                
                When the clustering is done, a window showing the clusters that were found is opened. Clicking on each 
                cluster in this window, highlights the sequences that belong to the selected cluster in the 3D graph. 
                Selecting a cluster and then clicking the <span style="color:#2E8C81;">Add to sequence groups</span> button, defines the sequences belong to 
                this cluster as a group that can be edited by selecting the <span style="color:#2E8C81;">Window -&gt; Edit Groups</span> option. 
                Selecting all the sequences and clicking the <span style="color:#2E8C81;">Add each as separate sequence group</span> button, defines the 
                sequences that belong to each cluster as a different group that can be edited in the <span style="color:#2E8C81;">Edit Groups</span> window. 
                Once the clusters are defined as separate sequence groups, this information will be written to a CLANS 
                saved-file (inside a &lt;seqgroups&gt; block), including the numbers of the sequences composing 
                each group (=cluster) and each group’s attributes.
                
                <li>
                    <b>Get sequence with hits from/to selected</b>: this option should be selected after selecting sequences 
                    from the graph. Then, a window composed of two parts is opened. On the left side, displayed the names 
                    of the selected sequences. On the right side, the names of the sequences, having BLAST hits from/to the 
                    selected sequences. Clicking the OK button highlights the sequences from the right side of the window 
                    in the 3D graph. Selecting sequences (from both sides) and clicking the <span style="color:#2E8C81;">Set as selected</span> button, 
                    sets these sequences as selected in the graph.
                </li>
                <li>
                    <b>Show selected sequences as text (copy/pastable)</b>: open a window displaying the selected sequences in FASTA format.
                </li>
                <li>
                    <b>Rotation</b>: the rotation window enables to set a rotation angle for a continuous or discrete rotation around the X or Y axes.
                        <ul>
                            <li>
                                <b>X</b>: set a rotation angle for a rotation around the Y axis.
                            </li>
                            <li>
                                <b>Y</b>: set a rotation angle for a rotation around the X axis.
                            </li>
                            <li>
                                <b>Time (min. ms)</b>: define the time (in milliseconds) between each rotation, when checking the 
                                <span style="color:#2E8C81;">continuous rotate</span> option (the lower the value, the faster the rotation).
                            </li>
                        </ul>
                </ul><br>
        <h5><b>Generating an image of CLANS map</b></h5>
        Once the clustering process has reached its desired state, you would probably want to save the obtained 
        CLANS map as a high-resolution image. Since CLANS has no built-in ‘export to image format’ function, it 
        is required to make a screenshot. The following steps may help you generating a high-resolution image 
        out of your CLANS map:
        <ol>
            <li>
                For the purpose of generating an image, it is recommended to perform the clustering in 2D instead of 3D 
                (<span style="color:#2E8C81;">Misc -&gt; Cluster in 2D</span>).
            </li>
            <li>
            Once you are satisfied with the clustering, set all the visual features (size and colors of dots 
            and lines, groups features, etc.) and position the graph as desired.
            </li>
            <li>
            Turn the anti-aliasing feature on (<span style="color:#2E8C81;">Draw -&gt; Antialiasing</span>) to make the graph look smoother.
            </li>
            <li>
            Before you make a screenshot, adjust the dimensions of the CLANS window as desired and make sure that 
            your display resolution is optimal (the resolution of a screenshot image depends on the display resolution).
            </li>
            <li>
            Capture a screenshot of the CLANS map (in Windows: click on the CLANS window and then use ALT + PrtScn 
            to copy it to the clipboard. In Mac: use command + shift + 4 and then drag the mouse to select the screen 
            area to capture. The image is then saved on the desktop and can be copied to the clipboard and pasted 
            to another program).
            </li>
            <li>
            Paste the image from the clipboard to an image manipulation program like Photoshop or GIMP, and edit it as desired.
            </li>
        </ol>
                `,
                parameters: [
                    {
                        title: 'Input',
                        content: `@:toolHelpModals.common.multiseq`,
                    },
                    {
                        title: 'Scoring Matrix',
                        content: `Specify the scoring matrix that is used for PSI-BLAST.`,
                    },
                    {
                        title: 'Extract BLAST HSP\'s up to E-values of',
                        content: `Specifies the cut-off value for BLAST E-values. HSPs with E-value larger than this are not being
                    extracted.`,
                    },
                ],
                references: `<p>Frickey T., Lupas AN. (2004) <b>CLANS: a Java application for visualizing protein families based on
        pairwise similarity. </b>Bioinformatics 20(18):3702-3704.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/15284097 target="_blank" rel="noopener">PMID: 15284097</a></p>`,
            },
        },
    },
};
