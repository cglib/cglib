package net.sf.cglib.transform;

import java.io.File;
import java.util.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

abstract public class AbstractProcessTask extends Task {
    private Vector filesets = new Vector();

    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    
    private Collection getFiles() {
        Map fileMap = new HashMap();
        Project p = getProject();
        for (int i = 0; i < filesets.size(); i++) {
            FileSet fs = (FileSet)filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(p);
            String[] srcFiles = ds.getIncludedFiles();
            File dir = fs.getDir(p);
            for (int j = 0; j < srcFiles.length; j++) {
                File src = new File(dir, srcFiles[j]);
                fileMap.put(src.getAbsolutePath(), src);
            }
        }
        return fileMap.values();
    }

    public void execute() throws BuildException {
        init();
        for (Iterator it = getFiles().iterator(); it.hasNext();) {
            try {
                processFile((File)it.next());
            } catch (Exception e) {
                throw new BuildException(e);
            }
        }
    }

    abstract protected void processFile(File file) throws Exception;
}
