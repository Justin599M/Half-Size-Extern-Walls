import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class SmallerExterns {
    public void RunTheClass(){                              //TODO make this accept a path and something to return
        String InputFilePath = "C:/Users/justi/Downloads/Export.gcode";
        String OutputFilePath = "C:/Users/justi/Downloads/ProcessedExport.gcode";
        File InputFile = new File(InputFilePath);
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
        float LayerHeight;          //get diffrence of last LayerHeight to account for variable height
        //
        String TempString = "";
        String ExitKey = "M204";
        DecimalFormat NoScientific = new DecimalFormat("#.#########################");
        try {
            InputFileScanner = new Scanner(InputFile);
        } catch (FileNotFoundException e) {throw new RuntimeException(e);}

        while (InputFileScanner.hasNextLine()){
            CurrentLine = InputFileScanner.nextLine();
            WriteLines.add(CurrentLine);
            LineIndex++;
            if (Objects.equals(CurrentLine,";TYPE:External perimeter")){    //TODO || OverhangPerimeter
                LayerHeight = LastZ - ZBeforeLastZ; //This line allows variable layer height to work with this
                while (true){
                    CurrentLine = InputFileScanner.nextLine();
                    LineIndex++;
                    WriteLines.add(CurrentLine);
                    SplitCurrentLine = CurrentLine.split(" ");
                    if (Objects.equals(SplitCurrentLine[0],"G1")){
                        break;          //TODO capture F value here.
                    }
                }
                SavedLines = new ArrayList<>();
                XValues = new ArrayList<>();
                YValues = new ArrayList<>();
                EValues = new ArrayList<>();
                while (true){
                    CurrentLine = InputFileScanner.nextLine();
                    LineIndex++;
                    WriteLines.add(CurrentLine);            //TODO outdated how I delete the old WriteLines, update so I only write if not an Extern Perimeter
                    SplitCurrentLine = CurrentLine.split(" ");
                    if (Objects.equals(SplitCurrentLine[0],ExitKey)){
                        TempString = CurrentLine;
                        break;
                    }
                    if (!Objects.equals(SplitCurrentLine[0],"G1") || SplitCurrentLine.length < 2){
                        XValues.add(-4000f);
                        YValues.add(-4000f);
                        EValues.add(-4000f);
                        SavedLines.add(CurrentLine);
                        continue;
                    }
                    SavedLines.add("");
                    SplitIndex = 1;
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
                        CurrentLine = CurrentLine + " E" + NoScientific.format(EValues.get(ForIndex) / 2);
                    }
                    if (XValues.get(ForIndex) == -4000f){
                        CurrentLine = SavedLines.get(ForIndex);
                    }
                    WriteLines.add(CurrentLine);
                }
                WriteLines.add("G1 Z" + LastZ);
                WriteLines.add("G1 X" + LastX + " Y" + LastY);                  //TODO reversed mode.
                for (int ForIndex = 0; ForIndex < XValues.size(); ForIndex++){  //add upper half of a perimeter
                    CurrentLine = "G1";
                    if (XValues.get(ForIndex) != -2500f) {
                        CurrentLine = CurrentLine + " X" + NoScientific.format(XValues.get(ForIndex));
                    }
                    if (YValues.get(ForIndex) != -2500f) {
                        CurrentLine = CurrentLine + " Y" + NoScientific.format(YValues.get(ForIndex));
                    }
                    if (EValues.get(ForIndex) != -2500f) {

                        CurrentLine = CurrentLine + " E" + NoScientific.format(EValues.get(ForIndex) / 2);
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
            if (Objects.equals(SplitCurrentLine[0], "G1")){
                SplitIndex = 1;
                if (SplitIndex < SplitCurrentLine.length){
                    if (Objects.equals(SplitCurrentLine[SplitIndex].charAt(0),'X')){
                        CurrentLine = SplitCurrentLine[SplitIndex];
                        CurrentLine = CurrentLine.replace('X',' ');
                        LastX = Float.parseFloat(CurrentLine);
                        SplitIndex++;
                    }
                }
                if (SplitIndex < SplitCurrentLine.length){                                      //TODO see if I can find a better way to do the SplitCurrentLine if statements
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
                if (SplitIndex < SplitCurrentLine.length){
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
                }
            }
        }

        File WriteTo = new File(OutputFilePath);
        try {
            FileWriter TheFileWriter = new FileWriter(WriteTo);
            for (String writeLine : WriteLines) {
                if (!Objects.equals(writeLine, "G1")) {
                    TheFileWriter.write(writeLine + "\n");
                }
            }
            TheFileWriter.close();
            System.out.println("EXIT");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
