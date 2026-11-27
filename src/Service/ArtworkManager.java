package Service;

import DAO.ArtworkDAO;
import Model.Artwork;
import Model.User;
import Model.Request;
import java.util.*;

public class ArtworkManager {
    private ArtworkDAO dao = new ArtworkDAO();
    private Queue<Request> requestQueue = new LinkedList<>();

    public boolean addArtwork(Artwork artwork) {
        if (artwork == null || dao.getById(artwork.getId()) != null) {
            return false;
        }
        dao.add(artwork);
        return true;
    }

    public boolean updateArtwork(Artwork updatedArtwork) {
        if (updatedArtwork == null || dao.getById(updatedArtwork.getId()) == null) {
            return false;
        }
        dao.update(updatedArtwork.getId(), updatedArtwork.getTitle(), updatedArtwork.getArtist(), updatedArtwork.getPrice());
        return true;
    }

    public boolean removeArtwork(int id) {
        if (dao.getById(id) == null) {
            return false;
        }
        dao.delete(id);
        requestQueue.removeIf(request -> request.getArtwork().getId() == id);
        return true;
    }

    public Artwork getById(int id) {
        return dao.getById(id);
    }

    public List<Artwork> getByCategory(String category) {
        List<Artwork> result = new ArrayList<>();
        for (Artwork a : dao.getAll()) {
            if (a.getType().equalsIgnoreCase(category)) {
                result.add(a);
            }
        }
        return result;
    }

    public List<Artwork> getAllSortedNaturally() {
        List<Artwork> list = dao.getAll();
        Collections.sort(list);
        return list;
    }

    public List<Artwork> getAllSorted(Comparator<Artwork> comparator) {
        List<Artwork> list = dao.getAll();
        list.sort(comparator);
        return list;
    }

    public boolean submitRequest(User user, Artwork artwork) {
        if (user == null || artwork == null) {
            return false;
        }
        if (!user.getRole().equalsIgnoreCase("User")) {
            return false;
        }
        if (dao.getById(artwork.getId()) == null) {
            return false;
        }

        Request request = new Request(user, artwork);
        requestQueue.offer(request);
        System.out.println("Request submitted successfully!");
        return true;
    }

    public List<Request> getPendingRequests() {
        return new ArrayList<>(requestQueue);
    }

    public boolean processNextRequest(User admin, boolean approve) {
        // unchanged from your version
        if (admin == null || !admin.getRole().equalsIgnoreCase("Admin")) {
            System.out.println("Only admins can process requests.");
            return false;
        }

        Request request = requestQueue.poll();

        if (request == null) {
            System.out.println("There are no pending requests.");
            return false;
        }

        if (approve) {
            request.approve();
            System.out.println("Request approved!");
        } else {
            request.reject();
            System.out.println("Request rejected!");
        }

        System.out.println(request);
        return true;
    }

    public void printAnalytics() {
        System.out.println(" ARTWORK ANALYTICS: ");
        dao.printStatistics();
        System.out.println(" Pending requests: " + requestQueue.size());
        System.out.println("   ");
    }

    public int getArtworkCount() {
        return dao.getAll().size();
    }
}