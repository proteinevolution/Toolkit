export default {
    help: `
<div>
    <p>The MPI Bioinformatics Toolkit is a platform that integrates a great variety of
        tools for protein sequence analysis. Many tools are developed in-house, and several
        public tools are offered with extended functionality.
    </p>
    <p>The toolkit includes, among others: PSI-BLAST+, HHblits, HMMER;
        MUSCLE, MAFFT, ProbCons; HHrepID, PCOILS;HHpred, Modeller;
        CLANS, ANCESCON, PhyML; RetrieveSeq, HHfilter.
    </p>
</div>
<div class="section">
    <h6>Job submission</h6>
    <p>
        Each tool has a separate input page with a web form in which the user can input sequence data,
        upload sequence files, and specify options. All tools that take alignments
        as input accept FASTA and CLUSTAL formats. Upon submission, each job is assigned an unique
        identifier; You may also choose your own job-names to organize your work.
    </p>
</div>
<div class="section">
    <h6>Job management</h6>
    <p>
        Located on the left of the screen is a sidebar pane that holds a status
        list of all recent jobs in the current session. You can click
        on previously submitted jobs to check their status and view their results. An icon above the list
        provides access to the job manager, which allows easy searching and ordering of jobs.
    </p>
    <p>
        In general a session will last until the web browser is closed. However,
        because of different session implementations by the various browser types, jobs
        may occasionally disappear from the joblist. Therefore, noting down the job ids is
        recommended. Anonymous job results are stored for <strong>three
        weeks</strong>, whereas jobs of logged-in users are stored for <strong>three months</strong>.
    </p>
    <p>
        Please keep in mind, that the toolkit is focused on providing tools,
        not on storing results or other data: We don't make backups of your
        results. If the storage medium crashes or your job is deleted, we
        cannot restore it! After removal or updates of databases or tools, you
        probably will not be able to get the same results any more, even if
        you still know your input parameters. It's your task to save your
        results and databases.
    </p>
</div>
<div class="section">
    <h6>Inter-connectivity</h6>
    <p>
        Most of the tools in the Toolkit are
        interconnected, allowing job results of one tool to be forwarded as input to
        others. For example, you could run PSI-BLAST+, parse out a multiple alignment of selected
        hits and send the results to the cluster analysis tool CLANS.
    </p>
</div>
<div class="section">
    <h6>Reference</h6>
    <p>
        If you use our Toolkit for your research, please cite:
    </p>
    <p>
    @:citation
    </p>
</div>`,

    faq: `
<div class="section">
    <h6>How do I reference use of the MPI Bioinformatics Toolkit?</h6>
    <p>
        @:citation
    </p>
</div>
<div class="section">
    <h6>How can I get a key for MODELLER?</h6>
    <p>
        MODELLER is developed and maintained by the
        <a href="https://salilab.org/" target="_blank" rel="noopener">SALILAB</a>;
        a key can be obtained here:
        <a href="http://salilab.org/modeller/registration.shtml" target="_blank" rel="noopener">
            http://salilab.org/modeller/registration.shtml.
        </a>
    </p>
</div>
<div class="section">
    <h6>How long are my jobs stored for?</h6>
    <p>
       Anonymous job results are stored for a duration of three weeks,
       whereas jobs of logged-in users are stored for three months.
    </p>
</div>
<div class="section">
    <h6>How can I fetch back results of a jobI ran yesterday on my computer at home (I wasn't logged in)?</h6>
    <p>
         If you still remember the job ID, you can fetch your job back
         by typing the ID into the search box on the index page.
    </p>
</div>
<div class="section">
    <h6>My collaborator sent me the ID to a job he ran; how can I fetch his job?</h6>
    <p>
        You can fetch his job by typing the ID into the search box on the index page.
    </p>
</div>`,

    imprint: `
<div class="section">
    <h6>Provider Identification</h6>
    <p>
        The following provides mandatory data concerning the provider of this website,
        obligations with regard to data protection,
        as well as other important legal references involving the
        Internet site of the MPI Bioinformatics Toolkit
        (http://toolkit.tuebingen.mpg.de) as required by German law.
    </p>
</div>

<div class="section">
    <h6>Provider</h6>
    <p>
        The provider of this Internet site within the legal meaning
        of the term is the registered association Max Planck Society for the Advancement of Science e.V..
    </p>
</div>

<div class="section">
    <h6>Address</h6>
    <p>
        Max-Planck-Gesellschaft zur Foerderung der Wissenschaften e.V.<br>
        Hofgartenstrasse 8<br>
        80539 Munich<br>
        Germany<br>
        Phone: +49 89 2108-0<br>
        Internet: http://www.mpg.de
    </p>
</div>

<div class="section">
    <h6>Register of Societies and Associations</h6>
    <p>
        The Max Planck Society is registered in the Official Register of Societies and Associations at
        Berlin-Charlottenburg Local Court under the register number VR 13378 B.
    </p>
</div>

<div class="section">
    <h6>Representatives</h6>
    <p>
        The Max Planck Society is legally represented by its Board of Directors which, in turn,
        is represented by the President of the Society,
        Prof. Dr. Martin Stratmann and by Secretary General Rüdiger Willem.
    </p>
</div>

<div class="section">
    <h6>Value Added Tax Identification Number</h6>
    <p>
        The value added tax identification number of the Max Planck Society is DE 129517720.
    </p>
</div>

<div class="section">
    <h6>Editors</h6>
    <p>
        Responsible editors for the contents of the website of the MPI Bioinformatics Toolkit
        (http://toolkit.tuebingen.mpg.de) with regard to media law:
    </p>
    <p>
        Dr. Vikram Alva Kullanja<br>
        MPI for Developmental Biology<br>
        Spemannstr. 35<br>
        72076 Tübingen<br>
        vikram.alva[at]tuebingen.mpg.de
    </p>
</div>

<div class="section">
    <h6>Legal Structure</h6>
    <p>
        The Max Planck Society is a non-profit research facility which is organized as a registered association.
        All of the institutes and facilities of the Max Planck Society
        are largely autonomous in terms of organization and research,
        but as a rule have no legal capacity of their own.
    </p>
</div>

<div class="section">
    <h6>Foreign Language Pages</h6>
    <p>
        To the extent that parts of this Internet site are offered in languages other than German,
        this represents a service exclusively for staff and guests of the
        Max Planck Society who are not proficient in German.
    </p>
</div>

<div class="section">
    <h6>Liability for Contents of Online Information</h6>
    <p>
        As the provider of contents in accordance with Section 7 Paragraph 1 of the Tele-Media Law,
        the Max Planck Society shall be responsible for any contents which it makes
        available for use in accordance with general legal provisions.
        The Max Planck Society makes every effort to provide timely and accurate information on this Web site.
        Nevertheless, errors and inaccuracies cannot be completely ruled out.
        Therefore, the Max Planck Society does not assume any liability for the
        relevance, accuracy, completeness or quality of the information provided.
        The Max Planck Society shall not be liable for damage of a tangible or
        intangible nature caused directly or indirectly
        through the use or failure to use the information offered and/or
        through the use of faulty or incomplete information
        unless it is verifiably culpable of intent or gross negligence. The same shall
        apply to any downloadable software available free of charge.
        The Max Planck Society reserves the right to modify, supplement,
        or delete any or all of the information offered on its Internet site,
        or to temporarily or permanently cease publication thereof without prior and separate notification.
    </p>
</div>

<div class="section">
    <h6>Links to Internet Sites of Third Parties</h6>
    <p>
        This Internet site includes links to external pages.
    </p>
    <p>
        The respective provider shall be responsible for the contents of any linked external pages.
        In establishing the initial link,
        the Max Planck Society has reviewed the respective external content in order to
        determine whether such link entailed possible civilor criminal responsibility.
        However, a constant review of linked external pages is unreasonable without
        concrete reason to believe that a violation of the law may be involved.
        If the Max Planck Society determines such or it is pointed out by others that an external offer
        to which it is connected via a link entails civil or criminal responsibility,
        then the Max Planck Society will immediately eliminate any link to this offer.
        The Max Planck Society expressly dissociates itself from such contents.
    </p>
</div>

<div class="section">
    <h6>Copyright</h6>
    <p>
        The layout, graphics employed and any other contents on the homepage of the
        Max Planck Society Internet site are protected by copyright law.<br>
        © Max-Planck-Gesellschaft zur Förderung der Wissenschaften e.V., Munich. All rights reserved
    </p>
</div>`,

    contact: `
<div class="section">
    <p>
        Send your feature requests, comments or bug reports to:
        <a href="mailto:mpi-toolkit@tuebingen.mpg.de">
            mpi-toolkit@tuebingen.mpg.de
        </a>
    </p>
    <p>
        Please contact the <a href="http://www.mpibpc.mpg.de/soeding" target="_blank" rel="noopener">Södinglab</a>
        regarding issues pertaining to local installations of HHpred/HHblits.
    </p>
</div>`,

    cite: `
<div class="section">
    <p>
        If you use our Toolkit for your research, please cite us. It helps us keep this service running for you.
    </p>
 <p>@:citation</p>
</div>`,

    privacy: `
(<sup>*</sup>For English version, see below)<br><br>
Die Max-Planck-Gesellschaft zur Förderung der Wissenschaften e.V. (MPG) nimmt den Schutz Ihrer personenbezogenen Daten
sehr ernst. Wir verarbeiten personenbezogene Daten, die beim Besuch unserer Webseiten erhoben werden, unter Beachtung
der geltenden datenschutzrechtlichen Bestimmungen. Ihre Daten werden von uns weder veröffentlicht, noch unberechtigt
an Dritte weitergegeben. Im Folgenden erläutern wir, welche Daten wir während Ihres Besuches auf unseren Webseiten
erfassen und wie genau diese verwendet werden:

<div class="section">
    <h5>A. Allgemeine Angaben</h5><br>
    <h6>1. Umfang der Datenverarbeitung</h6>
    <p>
        Wir erheben und verwenden personenbezogene Nutzerdaten grundsätzlich nur, soweit dies zur Bereitstellung
        einer funktionsfähigen Website sowie unserer Inhalte und Leistungen erforderlich ist. Die Erhebung und
        Verwendung personenbezogener Daten unserer Nutzer erfolgt regelmäßig nach Einwilligung der Nutzer. Eine
        Ausnahme gilt in solchen Fällen,
        in denen die Verarbeitung der Daten durch gesetzliche Vorschriften gestattet ist.
    </p>


    <h6>2. Rechtsgrundlage der Datenverarbeitung</h6>
    <p>
        Soweit wir für Verarbeitungsvorgänge personenbezogener Daten eine
        Einwilligung der betroffenen Person einholen,
        dient Art. 6 Abs. 1 lit. a EU-Datenschutzgrundverordnung (DSGVO) als Rechtsgrundlage.
    </p>
    <p>
        Bei der Verarbeitung von personenbezogenen Daten, die zur Erfüllung eines Vertrages, dessen Vertragspartei die
        betroffene Person ist, erforderlich ist, dient Art. 6 Abs. 1 lit. b DSGVO als Rechtsgrundlage. Dies gilt
        auch für Verarbeitungsvorgänge, die zur Durchführung vorvertraglicher Maßnahmen erforderlich sind.
    </p>
    <p>
        Ist die Verarbeitung zur Wahrung eines berechtigten Interesses der MPG oder eines Dritten erforderlich und
        überwiegen die Interessen, Grundrechte und Grundfreiheiten des Betroffenen das erstgenannte Interesse nicht,
        so dient Art. 6 Abs. 1 lit. f DSGVO als Rechtsgrundlage für die Verarbeitung.
    </p>


    <h6>3. Datenlöschung und Speicherdauer</h6>
    <p>
        Die personenbezogenen Daten der betroffenen Person werden gelöscht oder gesperrt, sobald der Zweck der
        Speicherung entfällt. Eine Speicherung kann darüber hinaus erfolgen, wenn dies durch den europäischen oder
        nationalen Gesetzgeber in unionsrechtlichen Verordnungen, Gesetzen oder sonstigen Vorschriften, denen die
        MPG unterliegt, vorgesehen wurde. Eine Sperrung oder Löschung der Daten erfolgt auch dann, wenn eine durch
        die genannten Normen vorgeschriebene Speicherfrist abläuft, es sei denn, dass eine Erforderlichkeit zur
        weiteren Speicherung der Daten für einen Vertragsabschluss oder eine Vertragserfüllung besteht.
    </p>
    <h6>4. Kontaktdaten der Verantwortlichen</h6>
    <p>
        Verantwortlich im Sinne der Datenschutz-Grundverordnung und anderer nationaler Datenschutzgesetze sowie
        sonstiger datenschutzrechtlicher Bestimmungen ist die
    </p>
        Max-Planck-Gesellschaft zur Förderung der Wissenschaften e.V. (MPG)<br>
        Hofgartenstraße 8<br>
        D-80539 München<br>
        Telefon: +49 (89) 2108-0<br>
        Kontaktformular: <a href="https://www.mpg.de/kontakt/anfragen" target="_blank">
        https://www.mpg.de/kontakt/anfragen</a><br>
        Internet: <a href="https://www.mpg.de" target="_blank">https://www.mpg.de</a><br><br>

    <h6>5. Kontaktdaten der Datenschutzbeauftragten</h6>
    <p>Die Datenschutzbeauftragte der Verantwortlichen ist</p>
        Heidi Schuster<br>
        Hofgartenstraße 8<br>
        D-80539 München<br>
        Telefon: +49 (89) 2108-1554<br>
        <a href="mailto:datenschutz@mpg.de">datenschutz[at]mpg.de</a><br><br>
</div>

<div class="section">
<h5>B. Bereitstellung der Website und Erstellung von Logfiles</h5>
    <p>
        Bei jedem Aufruf unserer Webseite erfassen unsere Server und Applikationen automatisiert Daten und
        Informationen vom Computersystem des aufrufenden Rechners.
    </p>
    Folgende Daten werden vorübergehend erhoben:<br>
    <ul>
        <li>Ihre IP-Adresse</li>
        <li>Datum und Uhrzeit Ihres Zugriffs auf die Seite</li>
        <li>Adresse der aufgerufenen Seite</li>
        <li>Adresse der zuvor besuchten Webseite (Referrer)</li>
        <li>Name und Version Ihres Browsers/Betriebssystems (sofern übertragen)</li>
    </ul>
    <p>
        Die Daten werden in den Logfiles unsere Systeme gespeichert. Eine Speicherung dieser Daten zusammen mit
        anderen personenbezogenen Daten des Nutzers findet nicht statt.
    </p>
    <p>
        Rechtsgrundlage für die vorübergehende Speicherung der Daten und der Logfiles ist Art. 6 Abs. 1 lit. f DSGVO.
        Die Speicherung in Logfiles erfolgt, um die Funktionsfähigkeit der Website sicherzustellen. Zudem dienen uns
        die Daten zur Optimierung der Webseiten, zur Störungsbeseitigung und zur Sicherstellung der Sicherheit unserer
        informationstechnischen Systeme. In diesen Zwecken liegt auch unser berechtigtes Interesse an der
        Datenverarbeitung nach Art. 6 Abs. 1 lit. f DSGVO.
    </p>
    <p>
        Die Daten werden gelöscht, sobald sie für die Erreichung des Zweckes ihrer Erhebung nicht mehr erforderlich
        sind. Im Falle der Erfassung der Daten zur Bereitstellung der Website ist dies der Fall, wenn die jeweilige
        Sitzung beendet ist. Im Falle der Speicherung der Daten in Logfiles ist dies nach spätestens sieben Tagen
        der Fall. Eine darüberhinausgehende Speicherung ist möglich. In diesem Fall werden die IP-Adressen der
        Nutzer gelöscht oder verfremdet, sodass eine Zuordnung des aufrufenden Clients nicht mehr möglich ist.
    </p>
    <p>
        Die Erfassung der Daten zur Bereitstellung der Website und die Speicherung der Daten in Logfiles ist für
        den Betrieb der Webseite zwingend erforderlich. Es besteht folglich seitens des Nutzers keine
        Widerspruchsmöglichkeit.
    </p>
</div>

<div class="section">
    <h5>C. Verwendung von Cookies</h5>
    <p>
        Unsere Webseite verwendet Cookies. Bei Cookies handelt es sich um Textdateien, die im Internetbrowser bzw.
        vom Internetbrowser auf dem Computersystem des Nutzers gespeichert werden. Ruft ein Nutzer eine Website auf,
        so kann ein Cookie auf dem Betriebssystem des Nutzers gespeichert werden. Dieses Cookie enthält eine
        charakteristische Zeichenfolge, die eine eindeutige Identifizierung des Browsers beim erneuten Aufrufen
        der Website ermöglicht.
    </p>
    <p>
        Wir setzen Cookies ein, um unsere Website nutzerfreundlicher zu gestalten. Einige Elemente unserer Webseite
        erfordern es technisch, dass der aufrufende Browser auch nach einem Seitenwechsel identifiziert werden kann.
        In den Cookies werden dabei folgende Daten gespeichert und übermittelt:
    </p>
        <ul>
            <li>Spracheinstellungen (Lokalisation) des Browsers: Sessioncookie i18next</li>
            <li>Sitzungsdaten (Klickstrecke, aufgerufene Seiten, aktuelle Sprache, sowie ggf. Fehlermeldungen für
                Formulare: Sessioncookie MPG_session_r</li>
        </ul>
    <p>
        Beide Cookies werden nach dem Schließen der Sitzung gelöscht.
    </p>
    <p>
        Die Rechtsgrundlage für die Verarbeitung personenbezogener Daten unter Verwendung von Cookies ist
        Art. 6 Abs. 1 lit. f DSGVO. Der Zweck der Verwendung technisch notwendiger Cookies ist, die Nutzung von
        Websites für die Nutzer zu vereinfachen. Einige Funktionen unserer Webseite können ohne den Einsatz von
        Cookies nicht angeboten werden. Für diese ist es erforderlich, dass der Browser auch nach einem Seitenwechsel
        wiedererkannt wird. Für folgende Anwendungen benötigen wir Cookies:
    </p>
        <ul>
            <li>Übernahme der Spracheinstellung des Browsers:
            automatische Auswahl der Startseite und Rechtschreibprüfung</li>
            <li>Merken von eingegebenen Formulardaten: bei der seiteninternen Suche verwendete Begriffe, Eingaben
                im Kontaktformular (Abschnitt D)</li>
        </ul>
    <p>
        Die durch technisch notwendige Cookies erhobenen Nutzerdaten werden nicht zur Erstellung von Nutzerprofilen
        verwendet. In diesen Zwecken liegt auch unser berechtigtes Interesse in der Verarbeitung der personenbezogenen
        Daten nach Art. 6 Abs. 1 lit. f DSGVO.
    </p>
    <p>
        Cookies werden auf dem Rechner des Nutzers gespeichert und von diesem an unserer Seite übermittelt.
        Daher haben Sie als Nutzer auch die volle Kontrolle über die Verwendung von Cookies. Durch eine Änderung
        der Einstellungen in Ihrem Internetbrowser können Sie die
        Übertragung von Cookies deaktivieren oder einschränken.
        Bereits gespeicherte Cookies können jederzeit gelöscht werden. Dies kann auch automatisiert erfolgen.
        Werden Cookies für unsere Website deaktiviert, können möglicherweise nicht mehr alle Funktionen der
        Website vollumfänglich genutzt werden.
    </p>
    <p>
        Wir verwenden auf unserer Website darüber hinaus Cookies, die eine Analyse des Nutzungsverhaltens der
        Nutzer ermöglichen.
    </p>
</div>


<div class="section">
    <h5> D. Kontaktformular (Kontakt per E-Mail)</h5>
    <p>
        Auf unserer Webseite ist ein Kontaktformular vorhanden, welches für die elektronische Kontaktaufnahme genutzt
        werden kann. Nimmt ein Nutzer diese Möglichkeit wahr, so werden die in der Eingabemaske eingegeben Daten an
        uns übermittelt und gespeichert. Diese sind in der Regel Ihre E-Mail-Adresse, Name und Vornamen. Über die
        konkrete Verarbeitung der Daten informieren wir Sie im Rahmen des Nutzungsvorgangs und holen Ihre Einwilligung
        ein. Zudem wird auf diese Datenschutzerklärung verwiesen. Die Daten werden ausschließlich für die Verarbeitung
        der Konversation verwendet.
    </p>
    <p>
        Rechtsgrundlage für die Verarbeitung der Daten bei der Nutzung des Kontaktformulars ist bei Vorliegen einer
        Einwilligung des Nutzers Art. 6 Abs. 1 lit. a DSGVO. Die Verarbeitung der personenbezogenen Daten aus der
        Eingabemaske dient uns allein zur Bearbeitung der Kontaktaufnahme. Die Daten werden gelöscht, sobald sie für
        die Erreichung des Zweckes ihrer Erhebung nicht mehr erforderlich sind. Dies dann der Fall, wenn die jeweilige
        Konversation mit dem Nutzer beendet ist bzw. das Anliegen des Nutzers abschließend bearbeitet ist. Beendet
        ist die Konversation dann, wenn sich aus den Umständen entnehmen lässt, dass der betroffene Sachverhalt
        abschließend geklärt ist. Der Nutzer hat jederzeit die Möglichkeit, die Einwilligung zur Verarbeitung der
        personenbezogenen Daten gegenüber den aufgelisteten Ansprechpartnern zu widerrufen.
    </p>
</div>


<div class="section">
    <h5>E. Registrierung</h5>
    <p>
        Auf unseren Webseiten bieten wir Nutzern die Möglichkeit, sich unter Angabe personenbezogener Daten über
        eine Eingabemaske zu registrieren. In der Regel erheben wir Ihre E-Mail-Adresse, Name und Vornamen.
        Über die konkrete Verarbeitung der Daten informieren wir Sie im Rahmen des Registrierungsvorgangs und
        holen Ihre Einwilligung ein. Zudem wird auf diese Datenschutzerklärung verwiesen.
    </p>
    <p>
        Rechtsgrundlage für die Verarbeitung der Daten ist bei Vorliegen einer Einwilligung des Nutzers
        Art. 6 Abs. 1 lit. a DSGVO. Dient die Registrierung der Erfüllung eines Vertrages, dessen Vertragspartei
        der Nutzer ist oder der Durchführung vorvertraglicher Maßnahmen, so ist zusätzliche Rechtsgrundlage für
        die Verarbeitung der Daten Art. 6 Abs. 1 lit. b DSGVO. Eine Registrierung des Nutzers ist für das Bereithalten
        bestimmter Inhalte und Leistungen auf unserer Website bzw. zur
        Erfüllung eines Vertrages mit dem Nutzer oder zur
        Durchführung vorvertraglicher Maßnahmen erforderlich. Die Daten werden gelöscht, sobald sie für die Erreichung
        des Zweckes ihrer Erhebung nicht mehr erforderlich sind. Dies ist für die während des Registrierungsvorgangs
        erhobenen Daten der Fall, wenn die Registrierung auf unseren Webseiten aufgehoben oder abgeändert wird. Für
        den Registrierungsvorgang zur Erfüllung eines Vertrags oder zur Durchführung vorvertraglicher Maßnahmen
        ist dies dann der Fall, wenn die Daten für die Durchführung des Vertrages nicht mehr erforderlich sind.
        Auch nach Abschluss des Vertrags kann eine Erforderlichkeit, personenbezogene Daten des Vertragspartners
        zu speichern, bestehen, um vertraglichen oder gesetzlichen Verpflichtungen nachzukommen.
    </p>
    <p>
        Als Nutzer haben sie jederzeit die Möglichkeit, die Registrierung aufzulösen. Die über Sie gespeicherten
        Daten können Sie jederzeit abändern lassen, die Vorgehensweise ist beim konkreten Registrierungsvorgang
        näher beschrieben. Sind die Daten zur Erfüllung eines Vertrages oder zur Durchführung vorvertraglicher
        Maßnahmen erforderlich, ist eine vorzeitige Löschung der Daten nur möglich, soweit nicht vertragliche oder
        gesetzliche Verpflichtungen einer Löschung entgegenstehen.
    </p>
</div>

<div class="section">
    <h5>F. Datenübermittlung</h5>
    <p>
        Die Verwaltung und Speicherung Ihrer persönlichen Angaben erfolgt bei ausgewählten Diensten
    </p>
    <ul>
        <li>Kontaktformular (Abschnitt D)</li>
        <li>Registrierung für die Abonnementverwaltung „abo.mpg.de“ (Abschnitt E)</li>
    </ul>
    <p>
    Ihre personenbezogenen Daten werden nur in den gesetzlich erforderlichen Fällen bzw. zur Strafverfolgung
        aufgrund von Angriffen auf unsere Netzinfrastruktur an staatliche Einrichtungen und Behörden übermittelt.
        Eine Weitergabe zu anderen Zwecken an Dritte findet nicht statt.
    </p>

</div>

<div class="section">
    <h5>G. Recht der betroffenen Personen</h5>
        <p>
            Als betroffene Person, deren personenbezogene Daten im Rahmen der oben genannten Dienste erhoben werden,
            haben Sie grundsätzlich folgende Rechte, soweit in Einzelfällen keine gesetzlichen Ausnahmen zur
            Anwendung kommen:
        </p>
            <ul>
                <li>Auskunft (Art. 15 DS-GVO)</li>
                <li>Berichtigung (Art. 16 DS-GVO)</li>
                <li>Löschung (Art. 17 Abs. 1 DS-GVO)</li>
                <li>Einschränkung der Verarbeitung (Art. 18 DS-GVO)</li>
                <li>Datenübertragbarkeit (Art. 20 DS-GVO)</li>
                <li>Widerspruch gegen die Verarbeitung (Art. 21 DS-GVO)</li>
                <li>Widerruf der Einwilligung (Art. 7 Abs. 3 DS-GVO)</li>
                <li>Beschwerderecht bei der Aufsichtsbehörde (Art. 77 DS-GVO). Dies ist für die MPG das Bayerische
                    Landesamt für Datenschutzaufsicht, Postfach 606, 91511 Ansbach.</li>
            </ul>
</div>

<div class="section">
    <h5>Google Fonts</h5>
        <p>
            Wir binden die Schriftarten ("Google Fonts") des Anbieters Google LLC, 1600 Amphitheatre Parkway,
            Mountain View, CA 94043, USA, ein. Datenschutzerklärung: <a href="https://www.google.com/policies/privacy/"
            target="_blank" rel="noopener"> https://www.google.com/policies/privacy/</a>,
            Opt-Out: <a href="https://adssettings.google.com/authenticated" target="_blank" rel="noopener">
                https://adssettings.google.com/authenticated</a>.
        </p>
        <h5>Font Awesome</h5>
        <p>
            Wir binden die Schriftarten "Font Awesome" ein (<a href="https://fontawesome.com/privacy"
            target="_blank" rel="noopener"> https://fontawesome.com/privacy</a>).
        </p>
</div><br><hr><br>
<h3>Privacy Policy</h3>
<br>
The Max-Planck-Gesellschaft zur Förderung der Wissenschaften e.V. (MPG) takes the protection of your personal data very
seriously. We process personal data gathered when visiting our websites in compliance with applicable data protection
legislation. We neither publish your data nor transmit them to third parties on an unauthorized basis. In the following
section, we explain which data we record when you visit one of our websites, and exactly how they are utilized:

<div class="section">
    <h5>A. General information</h5><br>
    <h6>1. Scope of data processing</h6>
    <p>
        As a matter of principle, we gather and utilize users&apos; personal data only to the extent required to ensure
        the functioning of our website and of our contents and services. The gathering and utilization of our
        users&apos; personal data normally occurs after users have granted their consent. An exception occurs where
        data processing is legally permitted.
    </p>


    <h6>2. Legal basis of data processing</h6>
    <p>
        To the extent that permission of the affected individual is obtained for the processing of personal data,
        Article 6 (1) lit. a of the EU General Data Protection Regulation (GDPR) serves as the legal basis.
    </p>
    <p>
        In the processing of personal data to fulfil a contract whose contractual party is the individual affected,
        Article 6 (1) lit. b GDPR serves as the legal basis. This also applies to processing required to implement
        pre-contractual measures.
    </p>
    <p>
        If processing is required to safeguard the justified interest of the MPG or a third party and the interests,
        basic rights and basic freedoms of the affected individual do not outweigh the first-mentioned interest,
        Article 6 (1) lit. f GDPR serves as the basis for such processing.
    </p>


    <h6>3. Data deletion and storage duration</h6>
    <p>
        The affected individual&apos;s personal data are deleted or blocked as soon as the purpose of the storage
        ceases to apply. Storage can also occur if provided for by European or national legislators in EU
        regulations, acts or other legislation to which the MPG is subject. A blocking or deletion of data then
        occurs only if a storage period prescribed by one of the aforementioned norms expires, unless a necessity
        exists in relation to the further storage of the data for the arrangement of a contract or the fulfilment
        of a contract.
    </p>
    <h6>4. Contact details of the individuals responsible</h6>
    <p>
        The entity responsible in the meaning of the General Data Protection Regulation and other national data
        protection acts as well as other data protection legislation is the
    </p>
        Max-Planck-Gesellschaft zur Förderung der Wissenschaften e.V. (MPG)<br>
        Hofgartenstraße 8<br>
        D-80539 Munich<br>
        Telephone: +49 (89) 2108-0<br>
        Contact form: <a href="https://www.mpg.de/contact/requests" target="_blank">
        https://www.mpg.de/contact/requests</a><br>
        Internet: <a href="https://www.mpg.de/en" target="_blank">
        https://www.mpg.de</a><br><br>

    <h6>5. Data Protection Officer&apos;s contact details</h6>
    <p>The Data Protection Officer at the entity responsible is</p>
        Heidi Schuster<br>
        Hofgartenstraße 8<br>
        D-80539 Munich<br>
        Telefon: +49 (89) 2108-1554<br>
        <a href="mailto:datenschutz@mpg.de">datenschutz[at]mpg.de</a><br><br>
</div>


<div class="section">
<h5>B. Provision of the website and creation of log files</h5>
    <p>
        Each time you visit our website, our service and applications automatically record data and information from
        the computer system of the visiting computer.
    </p>
    The following data are gathered temporarily:<br>
    <ul>
        <li>Your IP address</li>
        <li>Date and time of your access to the website</li>
        <li>Address of the page visited</li>
        <li>Address of the previously visited website (<i>referrer</i>)</li>
        <li>Name and version of your browser/operating system (if transmitted)</li>
    </ul>
    <p>
        These data are stored in our systems&apos; log files. These data are not stored together with the user&apos;s
        other personal data.
    </p>
    <p>
        The legal basis for the temporary saving of data and log files is Article 6 (1) lit. f GDPR. Storage occurs in
        log files in order to ensure the website&apos;s functionality. The data also help us optimize the websites,
        eliminate malfunctions and ensure our IT system security. Our justified interest in data processing pursuant
        to Article 6 (1) lit. f GDPR also lies in such purposes.
    </p>
    <p>
        The data are deleted as soon as they are no longer required to achieve the purpose for which they were
        gathered. If data are gathered for the provision of the website, this is the case if the respective visit
        is ended. In the instance that data are stored in log files, this is the case after seven days at the latest.
        Storage above and beyond this period is possible. In this case, the users&apos; IP addresses are deleted or
        removed so they can no longer be allocated to the visiting client.
    </p>
    <p>
        The recording of data for the provision of the website and the storage of data in log files is essential to
        operate the website. As a consequence, users do not have an option to revoke such data recording.
    </p>
</div>

<div class="section">
    <h5>C. Utilization of cookies</h5>
    <p>
        Our website utilizes cookies. Cookies are text files stored in the Internet browser or by the Internet browser
        on the user&apos;s computer system. If a user visits a website, a cookie can be stored on the user&apos;s
        operating system. This cookie contains a sequence of characters enabling the browser to be clearly identified
        when visiting the website again.
    </p>
    <p>
        We deploy cookies to make our website more user-friendly. Some elements of our website also technically require
        the identification of the visiting browser after a change of page. The following data are saved and transmitted
        in the cookies:
    </p>
        <ul>
            <li>Language settings (localization) of the browser: session cookie i18next</li>
            <li>Session data (click path, pages visited, current language, and, where relevant, error messages for
            forms: session cookie MPG_session_r</li>
        </ul>
    <p>
        Both cookies are deleted when the session is closed.
    </p>
    <p>
        The legal basis for personal data processing while utilizing cookies is Article 6 (1) lit. f GDPR. The purpose
        of utilizing technically necessary cookies is to simplify the utilization of websites for users. Some of our
        website&apos;s functions cannot be offered without the utilization of cookies. For these, it is necessary that
        the browser can also be re-identified following a change of page. We require cookies for the following
        applications:
    </p>
        <ul>
            <li>Transferring the browser&apos;s language setting: automatic selection of the homepage and spelling</li>
            <li>Noting of form data entered: terms and entries in the contact form utilized in searches within the
            website (section <b>D</b>)</li>
        </ul>
    <p>
        User data gathered by technically necessary cookies are not utilized to prepare user profiles. Our justified
        interest in personal data processing pursuant to Article 6 (1) lit. f GDPR also lies in such purposes.
    </p>
    <p>
        Cookies are stored on the user&apos;s computer, which transmits them to our site. For this reason, you, as the
        user, also have full control over the utilization of cookies. You can deactivate or restrict the transmission
        of cookies through changing your Internet browser settings. Cookies that have already been saved can be deleted
        at any time. This can also occur automatically. If cookies for our website are deactivated, you may find not
        all of the website&apos;s functions can continue to be utilized in full.
    </p>
    <p>
        In addition, we also utilize cookies on our website to enable users&apos; utilization behaviour to be analyzed.
    </p>
</div>


<div class="section">
    <h5> D. Contact form (contact through E-Mail)</h5>
    <p>
        A contact E-mail is available on our website for the purposes of making contact electronically. If a user opts
        to email us, we save the data contained in the email. These are generally your email address, family name and
        first name. We inform you about the specific processing of the data and we obtain your consent as part
        of the utilization procedure. Reference is also made to this data protection statement. The data are utilized
        exclusively to process the conversation.
    </p>
    <p>
        The legal basis for the processing of data when utilizing the contact form (email) is the user&apos;s consent
        pursuant to Article 6 (1) lit. a GDPR. We employ the processing of personal data from the data entry form
        solely to process the initiation of the contact. The data are deleted as soon as they are no longer required
        for the purpose for which they were gathered. This occurs if the respective conversation with the user ends
        or if the user&apos;s issue has been processed conclusively. The conversation has ended if the circumstances
        suggest that the respective matter has been clarified conclusively. At any time, users can notify the listed
        contact partners that they are revoking their consent to the processing of personal data.
    </p>
</div>


<div class="section">
    <h5>E. Registration</h5>
    <p>
        On our websites, we offer users the option to register, entailing the entry of personal data in a data entry
        form. We generally gather your email address, family name and first name. We inform you about the specific
        processing of the data and we obtain your consent as part of the registration procedure. Reference is also
        made to this data protection statement.
    </p>
    <p>
        The legal basis for the processing of data is the user&apos;s consent pursuant to Article 6 (1) lit. a GDPR. If
        registration serves to fulfil a contract whose contractual party is the user or to implement pre-contractual
        measures, the additional legal basis for the processing of data is Article 6 (1) lit. b GDPR. Registering the
        user is necessary to provide certain contents and services on our website or to fulfil a contract with the user
        or to implement pre-contractual measures. The data are deleted as soon as they are no longer required to
        achieve the purpose for which they were gathered. This is the case for data gathered during the registration
        process if registration is cancelled or modified on our websites. For the registration process to fulfil a
        contract or to implement pre-contractual measures, this is the case if the data are no longer required to
        fulfil the contract. After the contract ends, it may be necessary to continue to store the contractual
        partner&apos;s personal data in order to fulfil contractual or statutory obligations.
    </p>
    <p>
        As a user, you can cancel the registration at any time. You can have the data saved in connection with yourself
        modified at any time. The procedure is described in more detail in the specific registration procedure. If
        the data are required to fulfil a contract or to implement pre-contractual measures, early deletion of the
        data is possible only to the extent that no contractual or statutory obligations prevent such deletion.
    </p>
</div>

<div class="section">
    <h5>F. Data transmission</h5>
    <p>
        The management and storage of your personal information occurs in the case of selected services
    </p>
    <ul>
        <li>Contact form (section <b>D</b>)</li>
        <li>Registering for subscription management (section <b>E</b>)</li>
    </ul>
    <p>
    Your personal data will only be conveyed to state institutions and authorities in legally essential cases or for
    criminal prosecution based on attacks on our network infrastructure. The data are not transmitted to third parties
    for other purposes.
    </p>

</div>

<div class="section">
    <h5>G. Rights of individuals affected</h5>
        <p>
            As an individual whose personal data are gathered as part of the aforementioned services, you have, in
            principle, the following rights, to the extent that no legal exceptions are applicable in individual cases:
        </p>
            <ul>
                <li>Information (Article 15 GDPR)</li>
                <li>Correction (Article 16 GDPR)</li>
                <li>Deletion (Article 17 (1) GDPR)</li>
                <li>Restriction of processing (Article 18 GDPR)</li>
                <li>Data transmission (Article 20 GDPR)</li>
                <li>Revocation of processing (Article 21 GDPR)</li>
                <li>Revocation of consent (Article 7 (3) GDPR)</li>
                <li>Right to complain to the regulator (Article 77 GDPR). For the MPG, this is the Bavarian Data
                Protection Authority (BayLDA), Postbox 606, 91511 Ansbach.</li>
            </ul>
</div>

<div class="section">
    <h5>Google Fonts</h5>
        <p>
            We use the typeface "Google Fonts" provided by Google LLC, 1600 Amphitheatre Parkway,
            Mountain View, CA 94043, USA, ein. Privacy Policy: <a href="https://www.google.com/policies/privacy/"
            target="_blank" rel="noopener"> https://www.google.com/policies/privacy/</a>,
            Opt-Out: <a href="https://adssettings.google.com/authenticated" target="_blank" rel="noopener">
                https://adssettings.google.com/authenticated</a>.
        </p>
        <h5>Font Awesome</h5>
        <p>
            We also use the typeface "Font Awesome" (<a href="https://fontawesome.com/privacy"
            target="_blank" rel="noopener"> https://fontawesome.com/privacy</a>).
        </p>
</div>`,
};
