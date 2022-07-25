package app1;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
// Import the Scanner class to read text files
import java.util.*;
import static java.util.Arrays.*;
import static java.util.stream.Collectors.toList;

public class app1 {

    static int boardSize = 0;
    static ArrayList<String> rowInput=new ArrayList<String>();
    static ArrayList<String> colInput=new ArrayList<String>();
    public static void main(String[] args) {
        try {
            File myObj = new File(args[0]);
            //File myObj = new File("example1.txt");
            Scanner myReader = new Scanner(myObj);
            //First input is board size
            boardSize = Integer.parseInt(myReader.nextLine());

            //Row Input
            for(int i = 0; i < boardSize; ++i){
                if(myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    data=RemapInput(data);
                    rowInput.add(data);
                  }
            }
            //Col Input
            for(int i = 0; i < boardSize; ++i){
                if(myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    data=RemapInput(data);
                    colInput.add(data);
                  }
            }
            myReader.close();
          } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        newPuzzle();
    }

    static String RemapInput(String data){
        data=data.replaceAll("[^a-zA-Z0-9]","");
        data=data.replaceAll("1","A");
        data=data.replaceAll("2","B");
        data=data.replaceAll("3","C");
        data=data.replaceAll("4","D");
        data=data.replaceAll("5","E");
        data=data.replaceAll("6","F");
        data=data.replaceAll("7","G");
        data=data.replaceAll("8","H");
        data=data.replaceAll("9","I");
        return data;
    }

    static void newPuzzle() {
        String[] rowData = new String[rowInput.size()];
        rowData = rowInput.toArray(rowData);
        String[] colData = new String[colInput.size()];
        colData = colInput.toArray(colData);

        List<List<BitSet>> cols, rows;
        rows = getCandidates(rowData);
        cols = getCandidates(colData);

        int numChanged;
        do {
            numChanged = reduceMutual(cols, rows);
            if (numChanged == -1) {
                System.out.println("NA");
                return;
            }
        } while (numChanged > 0);

        for (List<BitSet> row : rows) {
            for (int i = 0; i < cols.size(); i++)
                System.out.print(row.get(0).get(i) ? "x" : "-");
            System.out.println();
        }
        System.out.println();
    }

    // collect all possible solutions for the given clues
    static List<List<BitSet>> getCandidates(String[] data) {
        List<List<BitSet>> result = new ArrayList<>();

        for (String s : data) {
            List<BitSet> lst = new LinkedList<>();

            int sumChars = s.chars().map(c -> c - 'A' + 1).sum();
            List<String> prep = stream(s.split(""))
                    .map(x -> repeat(x.charAt(0) - 'A' + 1, "1"))
                    .collect(toList());

            for (String r : genSequence(prep, boardSize - sumChars + 1)) {
                char[] bits = r.substring(1).toCharArray();
                BitSet bitset = new BitSet(bits.length);
                for (int i = 0; i < bits.length; i++)
                    bitset.set(i, bits[i] == '1');
                lst.add(bitset);
            }
            result.add(lst);
        }
        return result;
    }

    // permutation generator
    static List<String> genSequence(List<String> ones, int numZeros) {
        if (ones.isEmpty())
            return asList(repeat(numZeros, "0"));

        List<String> result = new ArrayList<>();
        for (int x = 1; x < numZeros - ones.size() + 2; x++) {
            List<String> skipOne = ones.stream().skip(1).collect(toList());
            for (String tail : genSequence(skipOne, numZeros - x))
                result.add(repeat(x, "0") + ones.get(0) + tail);
        }
        return result;
    }

    static String repeat(int n, String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++)
            sb.append(s);
        return sb.toString();
    }

    /* If all the candidates for a row have a value in common for a certain cell,
    then it's the only possible outcome, and all the candidates from the
    corresponding column need to have that value for that cell too. The ones
    that don't, are removed. The same for all columns. It goes back and forth,
    until no more candidates can be removed or a list is empty (failure). */

    static int reduceMutual(List<List<BitSet>> cols, List<List<BitSet>> rows) {
        int countRemoved1 = reduce(cols, rows);
        if (countRemoved1 == -1)
            return -1;

        int countRemoved2 = reduce(rows, cols);
        if (countRemoved2 == -1)
            return -1;

        return countRemoved1 + countRemoved2;
    }

    static int reduce(List<List<BitSet>> a, List<List<BitSet>> b) {
        int countRemoved = 0;

        for (int i = 0; i < a.size(); i++) {

            BitSet commonOn = new BitSet();
            commonOn.set(0, b.size());
            BitSet commonOff = new BitSet();

            // determine which values all candidates of ai have in common
            for (BitSet candidate : a.get(i)) {
                commonOn.and(candidate);
                commonOff.or(candidate);
            }

            // remove from bj all candidates that don't share the forced values
            for (int j = 0; j < b.size(); j++) {
                final int fi = i, fj = j;

                if (b.get(j).removeIf(cnd -> (commonOn.get(fj) && !cnd.get(fi))
                        || (!commonOff.get(fj) && cnd.get(fi))))
                    countRemoved++;

                if (b.get(j).isEmpty())
                    return -1;
            }
        }
        return countRemoved;
    }
}