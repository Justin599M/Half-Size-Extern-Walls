import javax.sound.sampled.Line;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class SmallerExternPerimeter {
    public static String quoteremover(String input){
        String send = "";                                   //For some reason Windows adds double quotes around when you copy a filepath in the right click menu
        char look = '"';
        if (input.charAt(0) == look){
            int inputlen = input.length();
            int gg = inputlen -1;
            for (int ind = 1; ind < gg; ind++){
                send = send + input.charAt(ind);
            }
            return send;
        }else {
            return input;
        }
    }
    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Please enter a filepath as an argument.");
        }else {
        String FilePath = quoteremover(args[0]);
        File InputFile = new File(FilePath);
        Scanner InputFileScanner;
        int LineIndex = 0;
        ArrayList<Float> XValues;
        ArrayList<Float> YValues;
        ArrayList<Float> EValues;
        ArrayList<Float> FValues = new ArrayList<Float>();
        ArrayList<String> WriteLines = new ArrayList<String>();
        ArrayList<String> SavedLines;
        float LastX = 0;
        float LastY = 0;
        float ZBeforeLastZ = 0;
        float LastZ = 0;
        float LastE = 0;
        float LastF = 0;
        String CurrentLine;
        String[] SplitCurrentLine;
        int SplitIndex;
        float LayerHeight;
        String TempString = "";
        String ExitKey = "M204";
        DecimalFormat NoScientific = new DecimalFormat("#.#######");
        try {
            InputFileScanner = new Scanner(InputFile);
        } catch (FileNotFoundException e) {throw new RuntimeException(e);}

        while (InputFileScanner.hasNextLine()){
            CurrentLine = InputFileScanner.nextLine();
            WriteLines.add(CurrentLine);
            LineIndex++;
            if (Objects.equals(CurrentLine,";TYPE:External perimeter")){
                LayerHeight = LastZ - ZBeforeLastZ; //This line allows variable layer height to work with this
                while (true){
                    CurrentLine = InputFileScanner.nextLine();
                    LineIndex++;
                    WriteLines.add(CurrentLine);
                    SplitCurrentLine = CurrentLine.split(" ");
                    if (Objects.equals(SplitCurrentLine[0],"G1")){
                        break;
                    }
                }
                SavedLines = new ArrayList<>();
                XValues = new ArrayList<>();
                YValues = new ArrayList<>();
                EValues = new ArrayList<>();
                while (true){
                    CurrentLine = InputFileScanner.nextLine();
                    LineIndex++;
                    WriteLines.add(CurrentLine);
                    SplitCurrentLine = CurrentLine.split(" ");
                    if (Objects.equals(SplitCurrentLine[0],ExitKey)){
                        TempString = CurrentLine;
                        break;
                    }
                    if (!Objects.equals(SplitCurrentLine[0],"G1")){
                        XValues.add(-4000f);
                        YValues.add(-4000f);    //accounts for any line that is not a G1 before an external perimeter ends
                        EValues.add(-4000f);
                        SavedLines.add(CurrentLine);
                        continue;
                    }
                    SavedLines.add("");
                    SplitIndex = 1;                                 //store all external perimeter G1 values
                    if (Objects.equals(SplitCurrentLine[SplitIndex].charAt(0),'X')){
                        CurrentLine = SplitCurrentLine[SplitIndex];
                        CurrentLine = CurrentLine.replace('X',' ');
                        XValues.add(Float.parseFloat(CurrentLine));
                        SplitIndex++;
                    }else{
                        XValues.add(-2500f);
                    }
                    if (Objects.equals(SplitCurrentLine[SplitIndex].charAt(0),'Y')){
                        CurrentLine = SplitCurrentLine[SplitIndex];
                        CurrentLine = CurrentLine.replace('Y',' ');
                        YValues.add(Float.parseFloat(CurrentLine));
                        SplitIndex++;
                    }else{
                        YValues.add(-2500f);
                    }
                    if (Objects.equals(SplitCurrentLine[SplitIndex].charAt(0),'E')){
                        CurrentLine = SplitCurrentLine[SplitIndex];
                        CurrentLine = CurrentLine.replace('E',' ');
                        EValues.add(Float.valueOf(CurrentLine));
                    }else{
                        EValues.add(-2500f);
                    }
                }
                for (int ForIndex = 0;ForIndex <= XValues.size(); ForIndex++){   //clear old external perimeter
                    WriteLines.removeLast();
                }
                WriteLines.add("G1 Z" + (LastZ - (LayerHeight/2)));
                for (int ForIndex = 0;ForIndex < XValues.size(); ForIndex++){   //add lower half of a perimeter
                    CurrentLine = "G1";
                    if (XValues.get(ForIndex) != -2500f) {
                        CurrentLine = CurrentLine + " X" + NoScientific.format(XValues.get(ForIndex));
                    }
                    if (YValues.get(ForIndex) != -2500f) {
                        CurrentLine = CurrentLine + " Y" + NoScientific.format(YValues.get(ForIndex));
                    }
                    if (EValues.get(ForIndex) != -2500f) {
                        CurrentLine = CurrentLine + " E" + NoScientific.format(EValues.get(ForIndex) * 0.575);      //New flow rate variable: External Flow Rate.
                    }                                                                                                      //I found that by increasing it a little helps with overhangs
                    if (XValues.get(ForIndex) == -4000f){
                        CurrentLine = SavedLines.get(ForIndex);
                    }
                    WriteLines.add(CurrentLine);
                }
                WriteLines.add("G1 Z" + LastZ);
                WriteLines.add("G1 X" + LastX + " Y" + LastY);                  //This is a travel move to bring the nozzle back to the start of the perimeter
                for (int ForIndex = 0; ForIndex < XValues.size(); ForIndex++){  //add upper half of a perimeter
                    CurrentLine = "G1";
                    if (XValues.get(ForIndex) != -2500f) {
                        CurrentLine = CurrentLine + " X" + NoScientific.format(XValues.get(ForIndex));
                    }
                    if (YValues.get(ForIndex) != -2500f) {
                        CurrentLine = CurrentLine + " Y" + NoScientific.format(YValues.get(ForIndex));
                    }
                    if (EValues.get(ForIndex) != -2500f) {

                        CurrentLine = CurrentLine + " E" + NoScientific.format(EValues.get(ForIndex) * 0.575);
                    }
                    if (XValues.get(ForIndex) == -4000f){
                        CurrentLine = SavedLines.get(ForIndex);
                    }
                    WriteLines.add(CurrentLine);
                }
                WriteLines.add(TempString); //Add back the M204 Value
                XValues = new ArrayList<Float>();
                YValues = new ArrayList<Float>();
                EValues = new ArrayList<Float>();
                continue;
            }
            SplitCurrentLine = CurrentLine.split(" ");
            if (Objects.equals(SplitCurrentLine[0], "G1")){                         //This part keeps track of XYZ movement so the programs knows where each perimeter starts
                SplitIndex = 1;
                if (SplitIndex < SplitCurrentLine.length){
                    if (Objects.equals(SplitCurrentLine[SplitIndex].charAt(0),'X')){
                        CurrentLine = SplitCurrentLine[SplitIndex];
                        CurrentLine = CurrentLine.replace('X',' ');
                        LastX = Float.parseFloat(CurrentLine);
                        SplitIndex++;
                    }
                }
                if (SplitIndex < SplitCurrentLine.length){
                    if (Objects.equals(SplitCurrentLine[SplitIndex].charAt(0),'Y')){
                        CurrentLine = SplitCurrentLine[SplitIndex];
                        CurrentLine = CurrentLine.replace('Y',' ');
                        LastY = Float.parseFloat(CurrentLine);
                        SplitIndex++;
                    }
                }
                if (SplitIndex < SplitCurrentLine.length){
                    if (Objects.equals(SplitCurrentLine[SplitIndex].charAt(0),'Z')){
                        CurrentLine = SplitCurrentLine[SplitIndex];
                        CurrentLine = CurrentLine.replace('Z',' ');
                        if (!Objects.equals(Float.parseFloat(CurrentLine),LastZ)){
                            ZBeforeLastZ = LastZ;
                            LastZ = Float.parseFloat(CurrentLine);
                        }
                        SplitIndex++;
                    }
                }
                /*if (SplitIndex < SplitCurrentLine.length){
                    if (Objects.equals(SplitCurrentLine[SplitIndex].charAt(0),'E')){
                        CurrentLine = SplitCurrentLine[SplitIndex];
                        CurrentLine = CurrentLine.replace('E',' ');
                        LastE = Float.parseFloat(CurrentLine);
                        SplitIndex++;
                    }
                }
                if (SplitIndex < SplitCurrentLine.length){
                    if (Objects.equals(SplitCurrentLine[SplitIndex].charAt(0),'F')){
                        CurrentLine = SplitCurrentLine[SplitIndex];
                        CurrentLine = CurrentLine.replace('F',' ');
                        LastF = Float.parseFloat(CurrentLine);
                        SplitIndex++;
                    }
                }*/
            }
        }

        File WriteTo = new File(FilePath);
        try {
            FileWriter TheFileWriter = new FileWriter(WriteTo);
            TheFileWriter.write(";This GCode has been modified by SmallerExternPerimeters, by Justin Martin, Three Eyed Cat\n");
            for (String writeLine : WriteLines) {
                if (!Objects.equals(writeLine, "G1")) {
                    TheFileWriter.write(writeLine + "\n");
                }
            }
            TheFileWriter.close();
            System.out.println("File edit complete, exiting");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
         }
    }
}
