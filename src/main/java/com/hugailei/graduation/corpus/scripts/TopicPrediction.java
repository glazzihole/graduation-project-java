package com.hugailei.graduation.corpus.scripts;

import com.mayabot.mynlp.fasttext.FastText;
import com.mayabot.mynlp.fasttext.FloatStringPair;
import com.mayabot.mynlp.fasttext.ModelName;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author HU Gailei
 * @date 2018/12/18
 * <p>
 * description: 主题预测
 * </p>
 **/
public class TopicPrediction {

    private static final String TRAINNING_DIRECTORY_PATH = "E:\\毕业论文相关\\fasttext-train\\data";
    private static final String TRAINNING_FILE_PATH = "E:\\毕业论文相关\\fasttext-train\\train_file.txt";
    private static final String MODEL_FILE_PATH = "E:\\毕业论文相关\\fasttext-train\\model.bin";

    public static void main(String[] args) throws Exception{
//        createTrainFile();
//        train();
        String text = "" +
                "Chinese lifter Liao Hui broke men's 69-kilogram category world record with a total of 358 kilogram in the World Weightlifting Championships, eclipsing the former record by one kilogram. Olympic Games champion lifted 160kg in snatch and 198kg in clean and jerk to end with a total of 358kg. The former record was made by Bulgarian Galabin Boevski in 1999.. All rights reserved. The content (including but not limited to text, photo, multimedia information, etc) published in this site belongs to China Daily Information Co (CDIC). Without written authorization from CDIC, such content shall not be republished or used in any form. Note: Browsers with 1024*768 or higher resolution are suggested for this site."
                ;
        FastText fastText = FastText.loadModel(MODEL_FILE_PATH);
        List<FloatStringPair> predict = fastText.predict(Arrays.asList(text.split(" ")), 5);
        System.out.println(predict);

        // 什么对于C等同于B对于A?
//        predict = fastText.analogies("china","man","woman",5);
//        System.out.println(predict);
//
//        predict = fastText.nearestNeighbor("love",5);
//        System.out.println(predict);

//        trainAgain();
    }

    public static void createTrainFile() throws Exception{
        File trainFile = new File(TRAINNING_FILE_PATH);
        FileWriter fileWriter = new FileWriter(trainFile);

        File rootPath = new File(TRAINNING_DIRECTORY_PATH);
        File[] directoryArray = rootPath.listFiles();
        for (File directory : directoryArray) {
            if (directory.isDirectory()) {
                File[] fileArray = directory.listFiles();
                for (File file : fileArray) {
                    System.out.println(file.getAbsolutePath());
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        line = "__label__" + directory.getName() + " " + line;
                        fileWriter.write(line + "\r\n");
                        fileWriter.flush();
                    }
                }
            }
        }
        fileWriter.close();
        System.out.println("训练文件创建成功");
    }

    public static void train() throws Exception {
        System.out.println("开始训练");
        File file = new File(TRAINNING_FILE_PATH);
        FastText fastText = FastText.train(file, ModelName.sup);

        fastText.saveModel(MODEL_FILE_PATH);
        System.out.println("训练完成");
    }

    public static void trainAgain() throws Exception {
        System.out.println("开始利用模型进行二次标注");
        FastText fastText = FastText.loadModel(MODEL_FILE_PATH);
        File directory = new File("E:\\毕业论文相关\\fasttext-train\\temp\\coca-magazine");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(TRAINNING_FILE_PATH, true)));
        for (File file : directory.listFiles()) {
            if (file.getName().contains(".")) {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    List<FloatStringPair> predict = fastText.predict(Arrays.asList(line.split(" ")), 5);
                    String label = "";
                    for (FloatStringPair pair : predict) {
                        label = label + pair.second + " ";
                    }
                    System.out.println(label + line);
                    out.write(label + line + "\r\n");
                    out.flush();
                }
            }
        }
        System.out.println("标注完成");
    }

}
