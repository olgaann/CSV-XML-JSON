import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class Basket {
    private String[] products;
    private double[] prices;
    private int[] bin;
    private JSONObject object = new JSONObject();
    static final int QTY_OF_LINES = 3;
    static DecimalFormat dF = new DecimalFormat("0.00");


    public Basket() {
    }

    public Basket(String[] products, double[] prices) {
        this.products = products;
        this.prices = prices;
        this.bin = new int[products.length];
    }

    public Basket(String[] products, double[] prices, int[] bin) {
        this.products = products;
        this.prices = prices;
        this.bin = bin;
    }


    public void addToCart(int productNum, int amount) { //метод добавления amount штук продукта номер productNum в корзину;
        bin[productNum] += amount;
        bin[productNum] = Math.max(bin[productNum], 0);
    }

    public void printCart() { //метод вывода на экран покупательской корзины

        System.out.println("Ваша корзина: ");
        double sum = 0;
        for (int i = 0; i < bin.length; i++) {
            if (bin[i] != 0) {
                System.out.println(products[i] + " " + bin[i] + " шт. " + dF.format(prices[i]) + " руб/шт. " + (dF.format(bin[i] * prices[i])) + " руб. в сумме");
            }
            sum += bin[i] * prices[i];
        }
        System.out.println("Общая сумма покупок: " + dF.format(sum) + " руб.");
    }

    public void printListOfProducts() { //метод вывода на экран доступных товаров для покупки
        System.out.println("Список возможных товаров для покупки: ");
        for (int i = 0; i < products.length; i++) {
            System.out.println((i + 1) + ". " + products[i] + "  " + dF.format(prices[i]) + " руб./шт.");
        }
    }

    public void saveTxt(File textFile) throws FileNotFoundException { //метод сохранения корзины в текстовый файл
        PrintWriter writer = new PrintWriter(textFile);

        subSaveTxt(products, textFile, writer);
        subSaveTxt(convertPrices(prices), textFile, writer);
        subSaveTxt(convertBin(bin), textFile, writer);
        writer.close();

    }

    public <T> void subSaveTxt(T[] arr, File textFile, PrintWriter writer) throws FileNotFoundException { //доп.дженерик-метод, чтобы избежать копирования в методе saveTxt

        for (T elem : arr) {
            writer.print(elem);
            writer.append("*");
        }
        writer.append("\n");

    }

    static Basket loadFromTxtFile(File textFile) { // метод загрузки корзины из текстового файла

        String[] arrOfLines = new String[QTY_OF_LINES];

        try (BufferedReader reader = new BufferedReader(new FileReader(textFile))) {

            for (int i = 0; i < arrOfLines.length; i++) {
                arrOfLines[i] = reader.readLine();
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        List<String[]> listOfLines = Arrays.stream(arrOfLines)
                .map(s -> s.substring(0, (s.length() - 1)))
                .map(s -> s.split("\\*"))
                .collect(Collectors.toList());

        String[] productsNew = listOfLines.get(0);
        double[] pricesNew = Arrays.stream(listOfLines.get(1)).mapToDouble(Double::parseDouble).toArray();
        int[] binNew = Arrays.stream(listOfLines.get(2)).mapToInt(Integer::parseInt).toArray();

        return new Basket(productsNew, pricesNew, binNew);
    }

    public void saveJson(File textFile) { //метод сохранения корзины в файл json

        object.put("Ассортимент: ", subSaveJson(products));
        object.put("Цены: ", subSaveJson(convertPrices(prices)));
        object.put("Корзина: ", subSaveJson(convertBin(bin)));


        try (FileWriter writer = new FileWriter(textFile)) {
            writer.write(object.toJSONString());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> JSONArray subSaveJson(T[] arr) { //доп.дженерик-метод, чтобы избежать копирования в методе saveJson
        JSONArray result = new JSONArray();
        for (T elem : arr) {
            result.add(elem);
        }
        return result;
    }

    static Basket loadFromJson(File textFile) {
        JSONParser parser = new JSONParser();
        JSONArray productsJSON = new JSONArray();
        JSONArray pricesJSON = new JSONArray();
        JSONArray binJSON = new JSONArray();
        try {
            Object object = parser.parse(new FileReader(textFile));
            JSONObject jsonObject = (JSONObject) object;
            productsJSON = (JSONArray) jsonObject.get("Ассортимент: ");
            pricesJSON = (JSONArray) jsonObject.get("Цены: ");
            binJSON = (JSONArray) jsonObject.get("Корзина: ");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        String[] productsNew = new String[productsJSON.size()];
        Double[] pricesNewDouble = new Double[pricesJSON.size()];
        Long[] binNewLong = new Long[binJSON.size()];

        productsNew = jsonToArray(productsJSON, productsNew);
        pricesNewDouble = jsonToArray(pricesJSON, pricesNewDouble);
        double[] pricesNew = Arrays.stream(pricesNewDouble).mapToDouble(d -> d).toArray();
        binNewLong = jsonToArray(binJSON, binNewLong);
        int[] binNew = Arrays.stream(binNewLong).mapToLong(l -> l).mapToInt(l -> (int) l).toArray();

        return new Basket(productsNew, pricesNew, binNew);
    }

    public static <T> T[] jsonToArray(JSONArray jsonArray, T[] arr) { //"конвертирует" JSONArray в массив типа Т
        for (int i = 0; i < jsonArray.size(); i++) {
            arr[i] = (T) jsonArray.get(i);
        }
        return arr;
    }

    public static Double[] convertPrices(double[] prices) { // "конвертирует" массив double[] в Double[]
        Double[] pricesDouble = new Double[prices.length];
        int i = 0;
        for (double value : prices) {
            pricesDouble[i++] = Double.valueOf(value);
        }
        return pricesDouble;
    }

    public static Integer[] convertBin(int[] bin) { // "конвертирует" массив int[] в Integer[]
        Integer[] binInteger = new Integer[bin.length];
        int i = 0;
        for (int value : bin) {
            binInteger[i++] = Integer.valueOf(value);
        }
        return binInteger;
    }


}

