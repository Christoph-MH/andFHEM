/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class FileDialog {
    private static final String PARENT_DIR = "..";
    private final String TAG = getClass().getName();
    private final Context context;
    private String[] fileList;
    private File currentPath;
    private ListenerList<FileSelectedListener> fileListenerList = new ListenerList<>();
    private ListenerList<DirectorySelectedListener> dirListenerList = new ListenerList<>();
    private boolean selectDirectoryOption;
    private String fileEndsWith;
    private Optional<Predicate<File>> fileFilter = Optional.absent();


    public FileDialog(Context context, File initialPath) {
        this.context = context;

        checkNotNull(initialPath);
        if (!initialPath.exists() || !initialPath.canRead()) {
            initialPath = Environment.getExternalStorageDirectory();
        }

        loadFileList(initialPath);
    }

    private void loadFileList(File path) {
        this.currentPath = path;

        Predicate<File> predicate = sel -> {
            if (!sel.canRead()) {
                return false;
            } else if (selectDirectoryOption) {
                return sel.isDirectory();
            } else {
                boolean endsWith = fileEndsWith == null || sel.getName().toLowerCase(Locale.getDefault()).endsWith(fileEndsWith);
                return endsWith || sel.isDirectory();
            }
        };
        if (fileFilter.isPresent()) {
            predicate = Predicates.and(predicate, fileFilter.get());
        }

        List<String> result = newArrayList();
        if (path.exists()) {
            if (path.getParentFile() != null) result.add(PARENT_DIR);
            final Predicate<File> finalPredicate = predicate;
            FilenameFilter filter = (dir, filename) -> finalPredicate.apply(new File(dir, filename));
            String[] files = path.list(filter);

            if (files != null) {
                Collections.addAll(result, files);
            }

            Collections.sort(result);
        }
        fileList = result.toArray(new String[result.size()]);
    }

    public Dialog createFileDialog() {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(currentPath.getAbsolutePath());
        if (selectDirectoryOption) {
            builder.setPositiveButton("Select directory", (dialog12, which) -> {
                Log.d(TAG, currentPath.getPath());
                fireDirectorySelectedEvent(currentPath);
            });
        }

        builder.setItems(fileList, (dialog1, which) -> {
            String fileChosen = fileList[which];
            File chosenFile = getChosenFile(fileChosen);
            if (chosenFile.isDirectory()) {
                loadFileList(chosenFile);
                dialog1.cancel();
                dialog1.dismiss();
                showDialog();
            } else {
                fireFileSelectedEvent(chosenFile);
            }
        });

        dialog = builder.show();
        return dialog;
    }

    public FileDialog setFileFilter(Predicate<File> fileFilter) {
        this.fileFilter = Optional.fromNullable(fileFilter);
        return this;
    }

    public FileDialog addFileListener(FileSelectedListener listener) {
        fileListenerList.add(listener);
        return this;
    }

    @SuppressWarnings("unused")
    public void removeFileListener(FileSelectedListener listener) {
        fileListenerList.remove(listener);
    }

    @SuppressWarnings("unused")
    public void setSelectDirectoryOption(boolean selectDirectoryOption) {
        this.selectDirectoryOption = selectDirectoryOption;
    }

    @SuppressWarnings("unused")
    public void addDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.remove(listener);
    }

    /**
     * Show file dialog
     */
    public void showDialog() {
        createFileDialog().show();
    }

    private void fireFileSelectedEvent(final File file) {
        fileListenerList.fireEvent(listener -> listener.fileSelected(file));
    }

    private void fireDirectorySelectedEvent(final File directory) {
        dirListenerList.fireEvent(listener -> listener.directorySelected(directory));
    }

    private File getChosenFile(String fileChosen) {
        if (fileChosen.equals(PARENT_DIR)) return currentPath.getParentFile();
        else return new File(currentPath, fileChosen);
    }

    @SuppressWarnings("unused")
    public void setFileEndsWith(String fileEndsWith) {
        this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase(Locale.getDefault()) : null;
    }

    public interface FileSelectedListener {
        void fileSelected(File file);
    }

    public interface DirectorySelectedListener {
        void directorySelected(File directory);
    }

    private interface FireHandler<L> {
        void fireEvent(L listener);
    }

    class ListenerList<L> {
        private List<L> listenerList = newArrayList();

        public void add(L listener) {
            listenerList.add(listener);
        }

        public void fireEvent(FireHandler<L> fireHandler) {
            for (L l : newArrayList(listenerList)) {
                fireHandler.fireEvent(l);
            }
        }

        public void remove(L listener) {
            listenerList.remove(listener);
        }
    }
}

