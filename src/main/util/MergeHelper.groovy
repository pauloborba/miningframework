package main.util

import main.project.*
import java.util.regex.Pattern
import java.util.regex.Matcher

class MergeHelper {
    private static String CONFLICT_INDICATOR = "(CONFLICT)|(CONFLITO)"

    static public boolean hasMergeConflict (Project project, MergeCommit mergeCommit) {
        Process mergeSimulation = replayMergeScenario(project, mergeCommit)
        String mergeMessage = mergeSimulation.getText()
        
        Pattern pattern = Pattern.compile(CONFLICT_INDICATOR)
        Matcher m = pattern.matcher(mergeMessage)
       
        boolean result = m.find()

        Process returnToMaster = returnToMaster(project)
        returnToMaster.waitFor()
    
        return result
    }

    static private Process replayMergeScenario(Project project, MergeCommit mergeCommit) {
        Process checkoutLeft = ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', mergeCommit.getLeftSHA())
        checkoutLeft.waitFor()

        return ProcessRunner.runProcess(project.getPath(), 'git', 'merge', mergeCommit.getRightSHA())   
    }

    static private Process returnToMaster(Project project) {
        Process resetChanges = ProcessRunner.runProcess(project.getPath(), 'git', 'reset', '--hard')
        resetChanges.waitFor()

        return ProcessRunner.runProcess(project.getPath(), 'git', 'checkout', 'master')
    }

}