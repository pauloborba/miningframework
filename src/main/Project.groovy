class Project {
    
    private String name
    private String path
    private boolean remote

    public Project(String name, String path) {
        this.name = name
        this.path = path
        this.remote = path.startsWith('https://github.com/')
    }

    public ArrayList<MergeCommit> getMergeCommits(String sinceDate, String untilDate) {
        ArrayList<MergeCommit> mergeCommits = new ArrayList<MergeCommit>()

        Process gitLog = getProcessBuilder(sinceDate, untilDate)
            .directory(new File(path))
            .redirectErrorStream(true)
            .start()

        BufferedReader reader = new BufferedReader(new InputStreamReader(gitLog.getInputStream()))
        String line
        while((line = reader.readLine()) != null) {
            if (line.startsWith('commit')) {
                String SHA = line.split(' ')[1]

                String[] parents = reader.readLine().split(' ')
                String[] parentsSHA = Arrays.copyOfRange(parents, 1, parents.length)

                reader.readLine() // Author
                reader.readLine() // Date

                String ancestorSHA = getCommonAncestor(SHA, parentsSHA)
                MergeCommit mergeCommit = new MergeCommit(SHA, parentsSHA, ancestorSHA)
                mergeCommits.add(mergeCommit)
            }
        }
        
        if(mergeCommits.isEmpty())
            println "No merge commits."
        return mergeCommits
    }

    private String getCommonAncestor(mergeCommitSHA, parentsSHA) {
        ProcessBuilder builder = new ProcessBuilder('git', 'merge-base').directory(new File(path))
        if (parentsSHA.length > 2)
            builder.command().add('--octopus')
        for (parent in parentsSHA)
            builder.command().add(parent)

        Process gitMergeBase = builder.start()
        gitMergeBase.getInputStream().eachLine {
            return it
        }
    }

    private ProcessBuilder getProcessBuilder(String sinceDate, String untilDate) {
        if(!sinceDate.equals('') && !untilDate.equals(''))
            return new ProcessBuilder('git', '--no-pager', 'log', '--merges', "--since=\"${sinceDate}\"", "--until=\"${untilDate}\"")
        else if(!sinceDate.equals(''))
            return new ProcessBuilder('git', '--no-pager', 'log', '--merges', "--since=\"${sinceDate}\"")
        else if(!untilDate.equals(''))
            return new ProcessBuilder('git', '--no-pager', 'log', '--merges', "--until=\"${untilDate}\"")
        else
            return new ProcessBuilder('git', '--no-pager', 'log', '--merges')
    }

    public String getName() {
        return name
    }

    public void setName(String name) {
        this.name = name
    }

    public String getPath() {
        return path
    }   

    public void setPath(String path) {
        this.path = path
    }

    public boolean isRemote() {
        return remote
    }

    public void setRemote(boolean remote) {
        this.remote = remote
    }
}