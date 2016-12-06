
/**
 * @author uidp7273
 *
 */

import edu.princeton.cs.algs4.StdIn;
//import edu.princeton.cs.algs4.UF;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Percolation {

    // I will use the state as a mask to know when a cell
    // is open or not, for simplicity of initialization 0
    // means that cell is blocked

    // Everything is going to get transformed to a linear array
    private boolean[] stateBoth;
    private boolean[] stateTop;
    private WeightedQuickUnionUF wufObjectBoth;
    private WeightedQuickUnionUF wufObjectTop;
    private int nLength;

    // create n-by-n grid, with all sites blocked
    public Percolation(int n) {
        // Check bounds
        if (n < 1) {
            throw new java.lang.IllegalArgumentException();
        }

        // Add start and finish points to list of cells
        nLength = n;

        wufObjectBoth = new WeightedQuickUnionUF(n * (n + 2));
        stateBoth = new boolean[n * (n + 2)];

        // Somewhat wasteful but ok...
        wufObjectTop = new WeightedQuickUnionUF(n * (n + 2));
        stateTop = new boolean[n * (n + 2)];

        // We have to make some connections and openings
        // at initialization though
        for (int i = 0; i < nLength; i++) {
            // col goes from 1 to n, padding at 0 and n + 1
            // fill both
            openInternal(0, i, stateBoth, wufObjectBoth);
            openInternal(nLength + 1, i, stateBoth, wufObjectBoth);
            // also open top, of course there is no bottom row for this
            openInternal(0, i, stateTop, wufObjectTop);
        }
    }

    // open site (row, col) if it is not open already
    public void open(int row, int col) {
        // Check bounds
        if ((row < 1) || (col < 1) || (row > nLength) || (col > nLength)) {
            throw new java.lang.IndexOutOfBoundsException();
        }

        // Transform to proper indexes, row is ok though
        openInternal(row, col - 1, stateBoth, wufObjectBoth);
        openInternal(row, col - 1, stateTop, wufObjectTop);
    }

    private void openInternal(int row, int col, boolean[] state, WeightedQuickUnionUF uf) {
        // We need to recover original index
        int linIndex = getLinearIndex(row, col, nLength);
        // We have to check if it is opened or not against state
        boolean opened = state[linIndex];

        // If it is opened, no need to do anything, as it should have
        // been done before, if it is closed then we need to make the union
        if (!opened) {
            // Open this tile
            state[linIndex] = true;

            // However first we need to check if squares to
            // sides are themselves opened or not, we can
            // check one by one, and make an union iff the two
            // tiles are opened

            // Up and down pose no limit check issue, as we have up and
            // down padding as extra rows, for 'landing'
            int upIndex = getLinearIndex(row - 1, col, nLength);
            int downIndex = getLinearIndex(row + 1, col, nLength);
            // However side indexes do need limit checks, this is done in
            // function
            int leftIndex = getLinearIndex(row, col - 1, nLength);
            int rightIndex = getLinearIndex(row, col + 1, nLength);

            // I leave names for clarity, though assignment can be done in place
            int[] indexes;
            indexes = new int[4];

            indexes[0] = upIndex;
            indexes[1] = downIndex;
            indexes[2] = leftIndex;
            indexes[3] = rightIndex;

            for (int index : indexes) {
                // At this point, we already know that we are opened in our
                // tile, so only
                // check if other tiles are opened and connect and so on
                // Just in case, check so we dont make an union with self, also
                // saves ticks
                if (index != linIndex) {
                    if (state[index]) {
                        // Side tile is opened! Make the union
                        uf.union(linIndex, index);
                    }
                }
            }
        }
    }

    // is site (row, col) open?
    public boolean isOpen(int row, int col) {
        // Check bounds
        if ((row < 1) || (col < 1) || (row > nLength) || (col > nLength)) {
            throw new java.lang.IndexOutOfBoundsException();
        }

        // Get proper indexes
        return isOpenInternal(row, col - 1, stateBoth);
    }

    private boolean isOpenInternal(int row, int col, boolean[] state) {
        int index = getLinearIndex(row, col, nLength);
        return state[index];
    }

    public boolean isFull(int row, int col) {
        // Check bounds
        if ((row < 1) || (col < 1) || (row > nLength) || (col > nLength)) {
            throw new java.lang.IndexOutOfBoundsException();
        }

        return isFullInternal(row, col - 1);
    }

    // is site (row, col) full?
    private boolean isFullInternal(int row, int col) {
        int index = getLinearIndex(row, col, nLength);

        boolean full = false;

        // opened should be the same in both states
        if (isOpenInternal(row, col, stateBoth)) {
            // Index should also be the same in both
            int firstIndex = getLinearIndex(0, 1, nLength);
            // For an object to be full, it must be connected to the first index
            // on TOP
            // and to the last index on BOTH
            if (wufObjectTop.connected(firstIndex, index)) {
                full = true;
            }
        }
        return full;
    }

    // does the system percolate?
    public boolean percolates() {
        // We only need to check against the first and last elements
        int firstIndex = getLinearIndex(0, 1, nLength);
        int lastIndex = getLinearIndex(nLength + 1, nLength, nLength);

        return wufObjectBoth.connected(firstIndex, lastIndex);
    }

    // Memory map is row row row ... row, with first and
    // last rows the 'special' rows for landing
    private int getLinearIndex(int row, int col, int n) {
        // and do limits check, as saturation
        if (col <= 0) {
            col = 0;
        }
        if (col >= n) {
            col = n - 1;
        }
        // Also check row
        if (row <= 0) {
            row = 0;
        }
        if (row >= n + 1) {
            row = n + 1;
        }
        // this one is classical
        return n * row + col;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        int n = StdIn.readInt();
        Percolation per = new Percolation(n);

        while (!StdIn.isEmpty()) {
            int p = StdIn.readInt();
            int q = StdIn.readInt();

            per.open(p, q);
        }

        System.out.println(per.percolates());
    }

}
