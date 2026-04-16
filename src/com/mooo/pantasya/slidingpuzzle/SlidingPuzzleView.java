package com.mooo.pantasya.slidingpuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class SlidingPuzzleView extends View {

    private static class Tile {
        Bitmap bitmap;
        boolean isEmpty;
        Tile(Bitmap bitmap, boolean isEmpty) {
            this.bitmap = bitmap;
            this.isEmpty = isEmpty;
        }
    }

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<Tile> tiles = new ArrayList<>();
    private final Random random = new Random();

    private Bitmap sourceBitmap;
    private int gridSize = 3;
    private int emptyIndex = -1;
    private int tileSize;
    private int boardSize;

    public SlidingPuzzleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
        borderPaint.setColor(0xFF222222);
    }

    public void setGridSize(int size) {
        gridSize = Math.max(2, size);
        if (sourceBitmap != null) buildTiles();
        invalidate();
    }

    public void setSourceBitmap(Bitmap bitmap) {
        sourceBitmap = bitmap;
        buildTiles();
        invalidate();
    }

    public void shuffle() {
        if (tiles.isEmpty()) return;

        int moves = gridSize * gridSize * 20;
        for (int i = 0; i < moves; i++) {
            int[] neighbors = getNeighborIndices(emptyIndex);
            int swapIndex = neighbors[random.nextInt(neighbors.length)];
            Collections.swap(tiles, emptyIndex, swapIndex);
            emptyIndex = swapIndex;
        }
        invalidate();
    }

    private void buildTiles() {
        tiles.clear();
        if (sourceBitmap == null || getWidth() == 0 || getHeight() == 0) return;

        boardSize = Math.min(getWidth(), getHeight());
        tileSize = boardSize / gridSize;

        Bitmap scaled = Bitmap.createScaledBitmap(sourceBitmap, boardSize, boardSize, true);

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                boolean empty = (row == gridSize - 1 && col == gridSize - 1);
                Bitmap piece = empty ? null : Bitmap.createBitmap(
                        scaled, col * tileSize, row * tileSize, tileSize, tileSize);
                tiles.add(new Tile(piece, empty));
                if (empty) emptyIndex = tiles.size() - 1;
            }
        }
        shuffle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (sourceBitmap != null) buildTiles();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tiles.isEmpty()) return;

        int left = (getWidth() - boardSize) / 2;
        int top = (getHeight() - boardSize) / 2;

        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            int row = i / gridSize;
            int col = i % gridSize;
            int x = left + col * tileSize;
            int y = top + row * tileSize;

            if (!t.isEmpty && t.bitmap != null) {
                canvas.drawBitmap(t.bitmap, null, new Rect(x, y, x + tileSize, y + tileSize), null);
            }
            canvas.drawRect(x, y, x + tileSize, y + tileSize, borderPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || tiles.isEmpty()) return true;

        int left = (getWidth() - boardSize) / 2;
        int top = (getHeight() - boardSize) / 2;
        int col = (int) ((event.getX() - left) / tileSize);
        int row = (int) ((event.getY() - top) / tileSize);

        if (col < 0 || col >= gridSize || row < 0 || row >= gridSize) return true;

        int tapped = row * gridSize + col;
        if (isAdjacent(tapped, emptyIndex)) {
            Collections.swap(tiles, tapped, emptyIndex);
            emptyIndex = tapped;
            invalidate();
        }
        return true;
    }

    private boolean isAdjacent(int a, int b) {
        int ar = a / gridSize, ac = a % gridSize;
        int br = b / gridSize, bc = b % gridSize;
        return Math.abs(ar - br) + Math.abs(ac - bc) == 1;
    }

    private int[] getNeighborIndices(int index) {
        int row = index / gridSize;
        int col = index % gridSize;
        int[] temp = new int[4];
        int count = 0;

        if (row > 0) temp[count++] = index - gridSize;
        if (row < gridSize - 1) temp[count++] = index + gridSize;
        if (col > 0) temp[count++] = index - 1;
        if (col < gridSize - 1) temp[count++] = index + 1;

        int[] result = new int[count];
        System.arraycopy(temp, 0, result, 0, count);
        return result;
    }
}