package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

//panel
//own class extend sub class
public class ChessView extends JPanel {

    int originX = 55;
    int originY = 45;
    int cellSide = 60;

    Map<String, Image> keyNameValueImage = new HashMap<String, Image>();


    public ChessView() {
        String[] imageNames = {
                "Bishop-black",
                "Bishop-white",
                "King-black",
                "King-white",
                "Knight-black",
                "Knight-white",
                "Pawn-black",
                "Pawn-white",
                "Queen-black",
                "Queen-white",
                "Rook-black",
                "Rook-white"



        };


        try {
            for (String imgName : imageNames
            ) {
                Image img = loadImage(imgName+".png");
                keyNameValueImage.put(imgName,img);


            }
        } catch (Exception e) {
          e.printStackTrace();
        }



    }




//overwrite one method
@Override
protected void paintChildren(Graphics g) {
    super.paintChildren(g);
    Graphics2D g2 = (Graphics2D) g;

    drawBoard(g2);
    drawImage(g2,0,0,"Rook-black");
    drawImage(g2,0,1,"Pawn-black");


//        int cellSide = 600/8;
//        g2.fillRect(0,0,cellSide,cellSide);
//        g2.fillRect(2*cellSide,0,cellSide,cellSide);
//        g2.fillRect(4*cellSide,0,cellSide,cellSide);
//        g2.fillRect(6*cellSide,0,cellSide,cellSide);
//        g2.fillRect(8*cellSide,0,cellSide,cellSide);

   /*     for(    int j = 0;    j<4;j++)

        {
//        g2.setColor(Color.white); //default color is black

        for (int i = 0; i < 4; i++) {
//            g2.fillRect(originX + (i * 2) * cellSide, originY + (j * 2) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i,2*j,true);
//            g2.fillRect(originX + (i * 2 + 1) * cellSide, originY + (j * 2 + 1) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i+1,2*j+1,true);
            drawSquare(g2,2*i+1,2*j,false);
            drawSquare(g2,2*i,2*j+1,false);
        }


//        g2.setColor(Color.gray); //default color is black

      /*
        for (int i = 0; i < 4; i++) {
//            g2.fillRect(originX +(i * 2 + 1) * cellSide, originY + (j * 2) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i+1,2*j,false);
//            g2.fillRect(originX + (i * 2) * cellSide, originY + (j * 2 + 1) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i,2*j+1,false);
        }


       */


}

private void drawImage(Graphics2D g2, int col, int row, String imgName){
    Image img = keyNameValueImage.get(imgName);
    g2.drawImage(img,originX+col*cellSide,originY+row*cellSide,cellSide,cellSide, null);

}

private Image loadImage(String imgFileName) throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    URL res = classLoader.getResource("img/"+imgFileName);
    if (res == null) {
        return null;
    } else {
        System.out.println("Yeah");

        File imgFile = new File(res.toURI());

        return ImageIO.read(imgFile);


    }


}


private void drawSquare(Graphics2D g2, int col, int row, boolean light) {
    g2.setColor(light ? Color.white : Color.gray);
    g2.fillRect(originX + col * cellSide, originY + row * cellSide, cellSide, cellSide);


}


private void drawBoard(Graphics2D g2) {
    for (int j = 0; j < 4; j++) {
//        g2.setColor(Color.white); //default color is black

        for (int i = 0; i < 4; i++) {
//            g2.fillRect(originX + (i * 2) * cellSide, originY + (j * 2) * cellSide, cellSide, cellSide);
            drawSquare(g2, 2 * i, 2 * j, true);
//            g2.fillRect(originX + (i * 2 + 1) * cellSide, originY + (j * 2 + 1) * cellSide, cellSide, cellSide);
            drawSquare(g2, 2 * i + 1, 2 * j + 1, true);
            drawSquare(g2, 2 * i + 1, 2 * j, false);
            drawSquare(g2, 2 * i, 2 * j + 1, false);
        }


//        g2.setColor(Color.gray); //default color is black

      /*
        for (int i = 0; i < 4; i++) {
//            g2.fillRect(originX +(i * 2 + 1) * cellSide, originY + (j * 2) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i+1,2*j,false);
//            g2.fillRect(originX + (i * 2) * cellSide, originY + (j * 2 + 1) * cellSide, cellSide, cellSide);
            drawSquare(g2,2*i,2*j+1,false);
        }


       */

    }
}


}
