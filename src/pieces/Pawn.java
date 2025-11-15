package pieces;
import java.util.Scanner;

import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece{
    //pentru piece type folositi varianta in engleza al cuvantului e.g. pawn, queen, rook, knight, king, bishop
    public Pawn(boolean isWhite) {
        super(isWhite, "pawn");
    }


    @Override
    public PiecePosition[] getMoves(Piece[][] piecesArray){
        List<PiecePosition> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int moveNumber = hasMoved() ? 1:2;
        int incrementY = isWhite ? -1 : 1;

        for(int i=1; i<=moveNumber; i++){
            if(canMove(x, y+incrementY*i, piecesArray)){
                currentMoves.add(new PiecePosition(x, y+incrementY*i));
            }else{
                break;
            }
        }

        return currentMoves.toArray(new PiecePosition[0]);
    }

    public boolean canPromote(){
        if(isWhite && (getPostion().y == 0)){
            return true;
        }

        return !isWhite && (getPostion().y == 7);
    }

    public void switchPiece(){
        Scanner sc= new Scanner(System.in);

//        if(canPromote()){
//            switch (sc.nextInt){
//                case 1:
//
//        }
    }

    /*
     queen, knight, rook, bishop
     */


}
