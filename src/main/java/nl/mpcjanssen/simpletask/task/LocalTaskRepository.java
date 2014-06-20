package nl.mpcjanssen.simpletask.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by janss484 on 1-6-2014.
 */
public interface LocalTaskRepository {

    abstract void init();

    abstract ArrayList<Task> load();

    abstract void store(ArrayList<Task> tasks);

    abstract boolean archive(ArrayList<Task> tasks, List<Task> tasksToArchive, String doneFile);

    abstract boolean todoFileModifiedSince(Date date);

    abstract File getTodoTxtFile();

}