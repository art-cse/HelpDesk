package helpdesk;

import java.util.ArrayList;

public class Registry<T extends Identifiable> {
    private final ArrayList<T> items;

    public Registry() {
        items = new ArrayList<T>();
    }

    public void add(T item) throws HelpDeskException {
        if (item == null) {
            throw new HelpDeskException("A null item cannot be registered.");
        }
        if (findById(item.getId()) != null) {
            throw new HelpDeskException("The ID '" + item.getId() + "' is already registered.");
        }
        items.add(item);
    }

    public T findById(String id) {
        if (id == null) {
            return null;
        }
        for (T item : items) {
            if (item.getId().equalsIgnoreCase(id.trim())) {
                return item;
            }
        }
        return null;
    }

    public T getRequired(String id, String itemName) throws HelpDeskException {
        T item = findById(id);
        if (item == null) {
            throw new HelpDeskException(itemName + " with ID '" + id + "' was not found.");
        }
        return item;
    }

    public ArrayList<T> getAll() {
        return new ArrayList<T>(items);
    }

    public int size() {
        return items.size();
    }
}
