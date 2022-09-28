import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    static String loadFilename;
    static String loadFormat;
    static String saveFilename;
    static String saveFormat;
    static String logFilename;

    public static void readSettingsFile() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File("shop.xml"));

        Node root = doc.getDocumentElement(); //получим корневой элемент
        NodeList nodeList = root.getChildNodes(); //получим список подкорневых элементов
        String block;
        String key;
        String value;
        for (int i = 0; i < nodeList.getLength(); i++) {
            //поочередно просматриваем секции уровнем ниже <config>
            Node node = nodeList.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType()) {
                block = node.getNodeName();
                Element block_element = (Element) node;
                if (!block_element.getElementsByTagName("enabled").item(0).getTextContent().equals("false")) {
                    NodeList nodeList2 = block_element.getChildNodes();
                    for (int j = 0; j < nodeList2.getLength(); j++) {
                        Node node2 = nodeList2.item(j); //
                        if (Node.ELEMENT_NODE == node2.getNodeType()) {
                            if (node2.getTextContent().equals("true")) {
                                continue;
                            }
                            key = node2.getNodeName(); //j=0 -> <enabled>, j=1 -> <fileName>, j=2 -> <format>
                            value = node2.getTextContent(); //текстовое наполенение
                            sendSettings(block, key, value);
                        }
                    }
                }
            }
        }
    }

    public static void sendSettings(String block, String key, String value) {
        switch (block) {
            case "load":
                switch (key) {
                    case "fileName":
                        loadFilename = value;
                        break;
                    case "format":
                        loadFormat = value;
                        break;
                }
                break;
            case "save":
                switch (key) {
                    case "fileName":
                        saveFilename = value;
                        break;
                    case "format":
                        saveFormat = value;
                        break;
                }
                break;
            case "log":
                logFilename = value;
                break;

        }
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

        String[] products = {"Йогурт", "Хлеб", "Яблоки", "Сыр"}; // массив товаров (ассортимента)
        double[] prices = {100.50, 75.00, 110.00, 800.00}; // массив цен
        Basket basket = new Basket(products, prices);
        Scanner scan = new Scanner(System.in);
        ClientLog myLog = new ClientLog();

        readSettingsFile(); //считываем настройки из файла shop.xml

        if (loadFilename != null) {
            File loadFile = new File(loadFilename);
            try {
                if (!loadFile.createNewFile() || loadFile.length() != 0L) {
                    if (loadFormat.equals("json")) {
                        basket = Basket.loadFromJson(loadFile);
                    } else if (loadFormat.equals("txt")) {
                        basket = Basket.loadFromTxtFile(loadFile);
                    }

                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }


        basket.printListOfProducts();
        while (true) {
            System.out.println("Введите номер товара и количество через пробел. Для завершения введите `end`");
            String input = scan.nextLine();

            if (input.equals("end")) {
                basket.printCart();
                break;
            }

            String[] purchase = input.split(" "); //расщепляем ввод пользователя на номер продукта и количество продукта
            if (purchase.length != 2) { //если пользователь сделал ввод не из двух частей
                System.out.println("Некорректный ввод. Введите два числа через пробел.");
                continue; //переходим к следующей итерации
            }

            int productNumber;
            int amount;

            try {
                productNumber = (Integer.parseInt(purchase[0])) - 1; //определяем номер продукта (в соотв.с ключом массива products)
                amount = Integer.parseInt(purchase[1]);
            } catch (NumberFormatException exception) {
                System.out.println("Ошибка преобразования значения!");
                continue;
            }

            if (productNumber < 0 || productNumber >= products.length) {
                System.out.println("Такого номера товара не существует.");
                continue;
            }
            basket.addToCart(productNumber, amount);
            myLog.log((productNumber + 1), amount);

            if (saveFilename != null) {
                File saveFile = new File(saveFilename);
                if (saveFormat.equals("json")) {
                    basket.saveJson(saveFile);
                } else if (saveFormat.equals("txt")) {
                    basket.saveTxt(saveFile);
                }
            }

        }
        scan.close();
        if (logFilename != null) {
            myLog.exportAsCSV(new File(logFilename));
        }

    }
}

