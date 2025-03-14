package org.ceskaexpedice.akubra.relsext;

/**
 * Callbacks about subtree processing
 * @author pavels, petrp
 */
public interface TreeNodeProcessor {

    /**
     * Process one object 
     * @param pid  PID of object
     * @param level Level of processing
     */
    public void process(String pid, int level);

    /**
     * Returns true if the processing algorithm should skip current branch 
     * @param pid OBject's pid to be processed
     * @param level current level
     * @return
     */
    public boolean skipBranch(String pid, int level);

    /**
     * Returns true means that processsing algorithm should stop processing. 
     * Calls after method process. 
     * @param pid  Current processed object's pid
     * @param level Level of processing
     * @return
     */
    public boolean breakProcessing(String pid, int level);

}
