package com.example.minedroid;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapManager {
    public enum GameDifficulty {
        EASY, MIDDLE, HARD
    }
    public enum GameState{
        WAIT,PLAYING,OVER
    }

    static final int[][] mapsize = {{9, 9}, {16, 16}, {16, 30}};
    static final int[] minecount = {10, 40, 99};
    MapItem[][] map = new MapItem[50][50];
    GameDifficulty difficulty;
    int width;
    int height;
    int count;
    int buttonwidth;
    int leftblock ;
    GameState gameState = GameState.WAIT;
    Activity context;
    public MapManager(Activity context, GameDifficulty difficulty) {
        this.context = context;
        this.difficulty = difficulty;
        width = mapsize[this.difficulty.ordinal()][0];
        height = mapsize[this.difficulty.ordinal()][1];
        count = minecount[this.difficulty.ordinal()];
        leftblock = height * width - count;
        buttonwidth = this.difficulty==GameDifficulty.EASY?40:25;

        for (int i = 0; i <= width + 1; i++) {
            for (int j = 0; j <= height + 1; j++) {
                map[i][j] = new MapItem(false);
                map[i][j].buttonState = MapItem.State.DEFAULT;
            }
        }
    }

    public static int b2i(boolean val) {
        return val ? 1 : 0;
    }
    private int getPixelsFromDp(int  size) {

        DisplayMetrics metrics = new DisplayMetrics();

        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return (size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;

    }
    public void generateMap() {

        for (int i = 0; i <= width + 1; i++) {
            for (int j = 0; j <= height + 1; j++) {
                map[i][j].setMine(false);
                map[i][j].buttonState= MapItem.State.DEFAULT;
            }
        }
        //生成地雷编号
        int tot = width * height;
        List<Integer> numlist = new ArrayList<>();
        for (int i = 0; i < tot; i++) numlist.add(i);
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(numlist.size());
            int ind = numlist.get(index);
            numlist.remove(index);
            map[(ind % width) + 1][(ind / width) + 1].setMine(true);
        }
        for (int i = 1; i <= width ; i++) {
            for (int j = 1; j <= height; j++) {
                //统计非地雷块周围地雷数目
                if (!map[i][j].isMine()) {
                    map[i][j].setMineCount(
                            b2i(map[i - 1][j + 1].isMine()) +
                            b2i(map[i][j + 1].isMine()) +
                            b2i(map[i + 1][j + 1].isMine()) +
                            b2i(map[i - 1][j].isMine()) +
                            b2i(map[i + 1][j].isMine()) +
                            b2i(map[i - 1][j - 1].isMine()) +
                            b2i(map[i][j - 1].isMine()) +
                            b2i(map[i + 1][j - 1].isMine())
                    );
                }
            }
        }
    }

    void gameWin() {
        Toast.makeText(context,"游戏胜利",Toast.LENGTH_SHORT ).show();
    }
    void extendBlockAt(int x, int y) {

        if(x == 0 || y == 0)return ;
        if(x == width + 1 || y == height + 1)return;
        if(map[x][y].buttonState != MapItem.State.DEFAULT)return;

        if(!map[x][y].isMine()) {
            map[x][y].setState(MapItem.State.OPENED);
            leftblock--;
            if(leftblock == 0) {
                gameWin();
            }
            if(map[x][y].getMineCount() == 0) {
                extendBlockAt(x,y - 1);
                extendBlockAt(x,y + 1);
                extendBlockAt(x - 1, y);
                extendBlockAt(x + 1, y);
                extendBlockAt(x - 1,y - 1);
                extendBlockAt(x + 1,y - 1);
                extendBlockAt(x - 1,y + 1);
                extendBlockAt(x + 1,y + 1);
            }
        } else {
            Toast.makeText(context,"游戏结束",Toast.LENGTH_SHORT ).show();
            gameState = GameState.OVER;
        }
    }

    void openBlockAround(int x, int y){
        if(x == 0 || y == 0)return ;
        if(x == width + 1 || y == height + 1)return;

        MapItem block = map[x][y];
        int flagCount = 0;

        for(int i = x - 1; i <= x + 1; i++){
            for(int j = y - 1; j <= y + 1; j++){
                if(i==x&&j==y){
                    continue;
                }
                if(map[i][j].getButtonState()== MapItem.State.FLAGED){
                    flagCount++;
                }
            }
        }

        if(block.getMineCount() == flagCount){
            extendBlockAt(x,y - 1);
            extendBlockAt(x,y + 1);
            extendBlockAt(x - 1, y);
            extendBlockAt(x + 1, y);
            extendBlockAt(x - 1,y - 1);
            extendBlockAt(x + 1,y - 1);
            extendBlockAt(x - 1,y + 1);
            extendBlockAt(x + 1,y + 1);
        }
    }

    public void generateButtons() {
        LinearLayout parent = (LinearLayout) context.findViewById(R.id.boxLayout);
        for (int j = 1; j <= height; j++) {
            LinearLayout ln = new LinearLayout(context);
            ln.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            ll.gravity= Gravity.CENTER;
            ln.setLayoutParams(ll);

            for (int i = 1; i <= width; i++) {
                Button b = new Button(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(getPixelsFromDp(buttonwidth),getPixelsFromDp(buttonwidth + 5));
                lp.setMargins(-6,-6,0,0);
                b.setLayoutParams(lp);
                b.setTag(new int[]{i, j});
                b.setPadding(1,1,1,1);
               // if (map[i][j].isMine)b.setText("雷");
             //   else b.setText(Integer.toString(map[i][j].getMineCount()));
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int[] pos = (int[])view.getTag();
                        int x = pos[0];
                        int y = pos[1];
                        switch (gameState){
                            case WAIT:
                                while (map[x][y].isMine())generateMap();
                                gameState = GameState.PLAYING;
                            case PLAYING:
                                switch (map[x][y].getButtonState()){
                                    case DEFAULT:
                                        extendBlockAt(x, y);
                                        break;
                                    case OPENED:
                                        openBlockAround(x, y);
                                        break;
                                    case FLAGED:
                                        break;
                                }
                                break;
                            case OVER:
                                break;
                        }
                     //   Toast.makeText(context,Integer.toString(pos[0])+","+Integer.toString(pos[1]),Toast.LENGTH_SHORT ).show();
                    }
                });
                b.setLongClickable(true);
                b.setOnLongClickListener(new View.OnLongClickListener() {
                                             @Override
                                             public boolean onLongClick(View view) {
                                                 int[] pos = (int[])view.getTag();
                                                // Toast.makeText(context,Integer.toString(pos[0])+","+Integer.toString(pos[1]),Toast.LENGTH_SHORT ).show();
                                                 if(map[pos[0]][pos[1]].buttonState == MapItem.State.DEFAULT ) {
                                                     map[pos[0]][pos[1]].setState(MapItem.State.FLAGED);
                                                 } else if(map[pos[0]][pos[1]].buttonState == MapItem.State.FLAGED) {
                                                     map[pos[0]][pos[1]].setState(MapItem.State.DEFAULT);
                                                 }
                                                 return true;
                                             }
                                         });
                ln.addView(b);
                map[i][j].viewButton = b;
            }
            parent.addView(ln);
        }
    }
}


