package pieces;
import java.util.Scanner;

import engine.ChessEngine;
import engine.Move;
import engine.PiecePosition;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece{
    boolean canEnpassant;


    public Pawn(boolean isWhite) {
        super(isWhite, "pawn");
    }

    public void setCanEnPassant(boolean canEnPassant){
        this.canEnpassant = canEnPassant;
    }

    public boolean getCanEnpassant(){
        return this.canEnpassant;
    }

    public boolean canEnPassant(int x, int y, Piece[][] piecesArray){
        if(x < 0 || x > 7 || y < 0 || y > 7){
            return false;
        }

        if(piecesArray[y][x] != null && piecesArray[y][x] instanceof Pawn pawn){
            return pawn.getCanEnpassant();
        }

        return false;
    }



    @Override
    public Move[] getMoves(Piece[][] piecesArray){
        List<Move> currentMoves = new ArrayList<>();

        int x = getPostion().x;
        int y = getPostion().y;

        int moveNumber = hasMoved() ? 1:2;
        int incrementY = isWhite ? -1 : 1;

        for(int i=1; i<=moveNumber; i++){
            int newY = y + incrementY*i;

            if(i == 1){
                if(canCapture(x+1, newY, piecesArray)){
                    currentMoves.add(new Move(x+1, newY, this, true));
                } else if (canEnPassant(x+1, y, piecesArray)) {
                    currentMoves.add(new Move(x+1, newY, this, true, true));
                }
                if(canCapture(x-1, newY, piecesArray)){
                    currentMoves.add(new Move(x-1, newY, this, true));
                }else if(canEnPassant(x-1, y, piecesArray)){
                    currentMoves.add(new Move(x-1, newY, this, true, true));
                }
            }

            if(canMove(x, newY, piecesArray)){
                currentMoves.add(new Move(x, newY, this));
            }else{
                break;
            }
        }

        return currentMoves.toArray(new Move[0]);
    }

}
