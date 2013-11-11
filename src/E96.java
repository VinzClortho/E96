/*
 Solution to Project Euler 96
 Jason LaFrance

 http://projecteuler.net/problem=96

 I just banged this one out with deductive recursion.  I sweep through the grid,
 generating a list of possible values at each unsolved point.  If there's only one possibility,
 then that's it.  If there's no possibilities, then back out of the current branch.  If there's
 two or more possibilities, try to follow these values through to a solution.

 My possibilities function also takes into account how many of each value are currently on the
 board, and builds the possible candidates list with the values which are nearly maxed out first
 and filters out any potential values that already are maxed out.

 The code's a bit long, but I wrote in a dirty little output window that shows the algorithm
 working on the puzzles.  This came in real handy for debugging.  Even with all the bloat,
 this still solves in under 10 seconds.
*/

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.InterruptedException;
import java.awt.*;
import javax.swing.*;

public class E96 {
    public static void main(String[] args) {
        int sum=0;
        JLabel [][] grid = createWindow();
        try {
            BufferedReader in = new BufferedReader(new FileReader("sudoku.txt"));

            int [][] board = new int[9][9];
            List<Integer> poss;
            int tally=0;
            for(int b=0; b<50; b++){
                String line;
                try {
                    line = in.readLine();
                    for(int i=0; i<9; i++){
                        line = in.readLine();
                        int val = Integer.parseInt(line);
                        for(int x=8; x>=0; x--){
                            board[i][x]=val%10;
                            val/=10;
                        }
                    }

                    resetBoard(grid, board);

                    System.out.println("Starting "+(b+1)+"... ("+tally+"/"+(b+1)+")");
                    board = solveBoard(grid, board);
                    resetBoard(grid, board);
                    sum+=board[0][0]*100;
                    sum+=board[0][1]*10;
                    sum+=board[0][2];

                } catch (IOException e) {}

            }

        } catch (FileNotFoundException e) {}
        System.out.println();
        System.out.println("Sum: "+sum);
    }

    public static JLabel[][] createWindow() {
        JFrame frame = new JFrame("Project Euler 96: Su Duko");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(9,11));

        JLabel [][] label = new JLabel[9][11];
        for(int y=0; y<9; y++){
            for(int x=0; x<11; x++){
                label[y][x] = new JLabel();
                label[y][x].setPreferredSize(new Dimension(30, 30));
                label[y][x].setText("");
                label[y][x].setOpaque(true);
                frame.getContentPane().add(label[y][x], BorderLayout.CENTER);
            }
        }

        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);

        return label;
    }

    public static int [][] solveBoard(JLabel [][] grid, int [][] board){
        int [][] backup = new int[9][9];
        for(int y=0; y<9; y++){
            for(int x=0; x<9; x++){
                backup[y][x]=board[y][x];
            }
        }
        List<Integer> p;

        while(countSolved(board)<81){
            int shortest = 100;
            for(int y=0; y<9; y++){
                for(int x=0; x<9; x++){
                    displayBoard(grid, board);
                    if(board[y][x]==0){
                        p = possibilities(board, x, y);

                        if (p.isEmpty()){
                            displayBoard(grid, backup);
                            return backup;
                        }

                        if(p.size()<shortest) shortest=p.size();

                        if(p.size()==1){
                            board[y][x] = p.get(0);
                            grid[y][x].setText(""+p.get(0));
                            grid[y][x].setBackground(Color.green);
                        }
                    }
                }
            }

            if(shortest>1){
                int xx=0, yy=0;
                while(true){
                    while(board[yy][xx]>0){
                        xx++;
                        if(xx==9){
                            xx=0;
                            yy++;
                            if(yy==9) {
                                displayBoard(grid, backup);
                                return backup;
                            }
                        }
                    }

                    p = possibilities(board, xx, yy);

                    if (p.isEmpty()){
                        displayBoard(grid, backup);
                        return backup;
                    }

                    grid[yy][xx].setBackground(Color.yellow);

                    int i=0;
                    while(i<p.size()){
                        board[yy][xx] = p.get(i);
                        board = solveBoard(grid, board);
                        if(countSolved(board)==81){
                            return board;
                        }
                        board[yy][xx] = 0;
                        i++;
                    }
                    grid[yy][xx].setText("0");
                    grid[yy][xx].setBackground(Color.white);
                    displayBoard(grid, backup);
                    return backup;
                }
            }
        }
        return board;
    }

    public static void resetBoard(JLabel [][] label, int [][] b){
        int [] o = new int[10];
        for(int y=0; y<9; y++){
            for(int x=0; x<9; x++){
                o[b[y][x]]++;
                label[y][x].setText(""+b[y][x]);
                if(b[y][x]==0) label[y][x].setBackground(Color.white);
                else label[y][x].setBackground(Color.green);
            }
        }
        for(int y=0; y<9; y++)
            label[y][10].setText((y+1)+"->"+o[y+1]);
    }

    public static void displayBoard(JLabel [][] label, int [][] b){
        int [] o = new int[10];
        for(int y=0; y<9; y++){
            for(int x=0; x<9; x++){
                o[b[y][x]]++;
                label[y][x].setText(""+b[y][x]);
                if(b[y][x]==0) label[y][x].setBackground(Color.white);
            }
        }
        for(int y=0; y<9; y++)
            label[y][10].setText((y+1)+"->"+o[y+1]);
    }

    public static List<Integer> possibilities(int [][] b, int x, int y){
        List<Integer> p = new ArrayList<>();
        boolean [] cand = new boolean[10];

        for(int i=0; i<10; i++) cand[i] = true;

        int yTop = y/3;
        yTop*=3;
        int xLeft = x/3;
        xLeft*=3;

        for(int yy=0; yy<3; yy++){
            for(int xx=0; xx<3; xx++){
                cand[b[yTop+yy][xLeft+xx]] = false;
            }
        }
        for(int yy=0; yy<9; yy++){
            cand[b[yy][x]]=false;
            cand[b[y][yy]]=false;
        }
        int [] o = new int[10];

        for(int yy=0; yy<9; yy++){
            for(int xx=0; xx<9; xx++){
                o[b[yy][xx]]++;
            }
        }

        cand[0]=false;
        o[0]=100;
        for(int j=0; j<9; j++){
            int h=0;
            for(int i=1; i<10; i++)
                if(o[i]<o[h]) h=i;
            if(cand[h]) p.add(h);
            o[h]=100;
        }
        return p;
    }

    public static int countSolved(int [][]b){
        int count=0;
        for(int y=0; y<9; y++){
            for(int x=0; x<9; x++){
                if(b[y][x]>0) count++;
            }
        }
        return count;
    }
}
