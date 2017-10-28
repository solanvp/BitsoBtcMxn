package com.solanvpnetworkers.bitso;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class BtcMxnFxApp extends Application {

    private static String FONT_FAMILY = "Lucida Sans Unicode";
    
    private ObservableList<String> bestAsksList; 
    private ObservableList<String> bestBidsList; 
    private TextFlow consolePane;
    
    private XYChart.Series series;
    private final int chartWindow = 30;
    
    private final int initX = 20; // 10 - 99
    private final int initM = 3;  // 1 - 20
    private final int initN = 3;  // 1 - 20
    
    // References to stop application on close
    private OrderBookManager orderManager;
    private TradeManager tradeManager;
    
    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        buildUI(primaryStage);
        
        orderManager = new OrderBookManager(this, initX);
        tradeManager = new TradeManager(this, initM, initN);
        
        orderManager.start();
        tradeManager.start();
        
    }
    
    @Override
    public void stop(){
        orderManager.stop();
        tradeManager.stop();
    }
    
    private void buildUI(Stage stage){
        stage.setTitle("BTC-MXN Bitso Mock");
		
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));

        // === X number of bids/asks display control === 
        HBox bidAskControlPane = new HBox();
        bidAskControlPane.setAlignment(Pos.CENTER_LEFT);
        bidAskControlPane.setSpacing(10);
        bidAskControlPane.setBackground(new Background(new BackgroundFill(Color.AZURE, CornerRadii.EMPTY, Insets.EMPTY)));
        bidAskControlPane.setPadding(new Insets(5, 5, 5, 20));
        bidAskControlPane.setStyle("-fx-border-color: darkgrey;");
        
        Label lblBestBidAsk = new Label("Displaying");
        lblBestBidAsk.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        Label lblBestBidAsk2 = new Label("best Bids and Asks [X]");
        lblBestBidAsk2.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        
        Spinner<Integer> orderDispSpinner = new Spinner<>();
        orderDispSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 99, initX));
        orderDispSpinner.setPrefWidth(70);
        orderDispSpinner.valueProperty().addListener(this::maxOrdersDisplayedChanged);
        
        bidAskControlPane.getChildren().addAll(lblBestBidAsk, orderDispSpinner, lblBestBidAsk2);

        grid.add(bidAskControlPane, 0, 0, 2, 1);
        
        Label lblBestBids = new Label("Best Bids");
        lblBestBids.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        grid.add(lblBestBids, 0, 1);
        
        Label lblBestAsks = new Label("Best Asks");
        lblBestAsks.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        grid.add(lblBestAsks, 1, 1);

        // === Bids & Aks lists ===
        ListView<String> list1 = new ListView<String>();
        list1.setMinWidth(280);
//        String[] array = {"Single", "Double", "Suite", "Family App"};
//        updateBestBidsAsksList(java.util.Arrays.asList(array), false);
        bestAsksList = FXCollections.observableArrayList();
        list1.setItems(bestAsksList);

        ListView<String> list2 = new ListView<String>();
        list2.setMinWidth(280);
        bestBidsList = FXCollections.observableArrayList();
        list2.setItems(bestBidsList);

        grid.add(list1, 0, 2, 1, 2);
        grid.add(list2, 1, 2, 1, 2);

        // === M number of upticks display control === 
        HBox uptickControlPane = new HBox();
        uptickControlPane.setAlignment(Pos.CENTER_LEFT);
        uptickControlPane.setSpacing(10);
        uptickControlPane.setBackground(new Background(new BackgroundFill(Color.AZURE, CornerRadii.EMPTY, Insets.EMPTY)));
        uptickControlPane.setPadding(new Insets(5, 5, 5, 20));
        uptickControlPane.setStyle("-fx-border-color: darkgrey;");
        
        Label lblUptick = new Label("Sell every");
        lblUptick.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        Label lblUptick2 = new Label("upticks [M]");
        lblUptick2.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        
        Spinner<Integer> tradeOnMuPticksSpinner = new Spinner<>();
        tradeOnMuPticksSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, initM));
        tradeOnMuPticksSpinner.setPrefWidth(70);
        tradeOnMuPticksSpinner.valueProperty().addListener(this::mUpticksChanged);
        
        uptickControlPane.getChildren().addAll(lblUptick, tradeOnMuPticksSpinner, lblUptick2);

        grid.add(uptickControlPane, 3, 0);

        // === N number of downticks display control === 
        HBox downtickControlPane = new HBox();
        downtickControlPane.setAlignment(Pos.CENTER_LEFT);
        downtickControlPane.setSpacing(10);
        downtickControlPane.setBackground(new Background(new BackgroundFill(Color.AZURE, CornerRadii.EMPTY, Insets.EMPTY)));
        downtickControlPane.setPadding(new Insets(5, 5, 5, 20));
        downtickControlPane.setStyle("-fx-border-color: darkgrey;");
        
        Label lblDowntick = new Label("Buy every");
        lblDowntick.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        Label lblDowntick2 = new Label("downticks [N]");
        lblDowntick2.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        
        Spinner<Integer> tradeOnNdOwnticksSpinner = new Spinner<>();
        tradeOnNdOwnticksSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, initN));
        tradeOnNdOwnticksSpinner.setPrefWidth(70);
        tradeOnNdOwnticksSpinner.valueProperty().addListener(this::nDownticksChanged);
        
        downtickControlPane.getChildren().addAll(lblDowntick, tradeOnNdOwnticksSpinner, lblDowntick2);

        grid.add(downtickControlPane, 3, 1);

        // === Chart === 
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(false);

        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override public String toString(Number object) {
                return new SimpleDateFormat("HH:mm:ss").format(new Date(object.longValue()));
            }
            @Override public Number fromString(String string) { return 0; }
        });

        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override public String toString(Number object) {
                Locale mx = new Locale("es", "MX");
                NumberFormat mxFormat = NumberFormat.getCurrencyInstance(mx);
                return mxFormat.format(object.doubleValue());
            }
            @Override public Number fromString(String string) { return 0; }
        });

        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setAnimated(false);
        lineChart.setLegendVisible(false);

        series = new XYChart.Series();
        lineChart.getData().add(series);
        series.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: grey;");

        grid.add(lineChart, 3, 2);

        // === Console ===
        
        ScrollPane consoleContainer = new ScrollPane();
        consolePane = new TextFlow();
        consolePane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        consoleContainer.setMinHeight(200);
        consoleContainer.setMaxHeight(200);
        consoleContainer.setMinWidth(300);
        
        consolePane.getChildren().addListener(
                    (ListChangeListener<Node>) ((change) -> {
                        consolePane.layout();
                        consoleContainer.layout();
                        consoleContainer.setVvalue(1.0f);
                    }));
        consoleContainer.setContent(consolePane);

        grid.add(consoleContainer, 3, 3);

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.show();
    }
    
    
    private void maxOrdersDisplayedChanged(ObservableValue o, Integer oldValue, Integer newValue){
        System.out.println("Max orders displayed changed: " + newValue);
        orderManager.setMaxOrdersDisplayed(newValue);
    }
    
    private void mUpticksChanged(ObservableValue o, Integer oldValue, Integer newValue){
        System.out.println("M upticks changed: " + newValue);
        tradeManager.setPlaceSellTradeOnMuPticks(newValue);
    }
    
    private void nDownticksChanged(ObservableValue o, Integer oldValue, Integer newValue){
        System.out.println("N downticks changed: " + newValue);
        tradeManager.setPlaceBuyTradeOnNdOwnticks(newValue);
    }
    
    
    public void updateBestBidsAsksList(List<String> plainList, boolean modBidList){
            Task<Void> task =  new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                            Platform.runLater(() -> {
                                if(modBidList){
                                    bestAsksList.clear();
                                    bestAsksList.addAll(plainList);
                                } else {
                                    bestBidsList.clear();
                                    bestBidsList.addAll(plainList);
                                }
                            });
                            return null;
                    }
            };
            task.run();
    }
    
    
    public void addTextToUiConsole(String msg, TradeType tradeType){
            Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                            Platform.runLater(() -> {
                                    Text consoleText = new Text(System.lineSeparator() + msg);
                                    consoleText.setFill(choseColor(tradeType));
                                    FontWeight f = tradeType == TradeType.BUY || tradeType == TradeType.SELL
                                            ? FontWeight.NORMAL : FontWeight.BOLD ;
                                    consoleText.setFont(Font.font("Courier", f, 12));
                                    consolePane.getChildren().add(consoleText);
                            });
                            return null;
                    }
            };
            task.run();
    }
    

    public void addDataToSeries(Long x, Double y, TradeType tradeType) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    XYChart.Data dt = new XYChart.Data(x, y);
                    
                    boolean inv = false; int scl = 1;
                    if(tradeType ==  TradeType.SELL || tradeType ==  TradeType.MOCK_SELL){
                        inv = true;
                    }
                    if(tradeType ==  TradeType.MOCK_BUY || tradeType == TradeType.MOCK_SELL){
                        scl = 2;
                    }
                    
                    Polygon triangle = createTriangle(inv, scl);
                    triangle.setFill(choseColor(tradeType));
                    
                    dt.setNode(triangle);

                    series.getData().add(dt);
                    if (series.getData().size() > chartWindow) {
                        series.getData().remove(0);
                    }

                });
                return null;
            }
        };
        task.run();
    }
    
    
    private Polygon createTriangle(boolean inverted, int scale) {
        Polygon triangle = new Polygon();
        
        if(inverted){
            triangle.getPoints().addAll(new Double[]{
                0.0, 0.0,
                12.0*scale, 0.0,
                6.0*scale, 10.0*scale});
        } else {
            triangle.getPoints().addAll(new Double[]{
                6.0*scale, 0.0,
                0.0, 10.0*scale,
                12.0*scale, 10.0*scale});
        }
        
        return triangle;
    }
    
    private Color choseColor(TradeType tradeType){
        switch(tradeType){
            case SELL:
                return Color.web("E98A49");
            case BUY:
                return Color.web("2E9382");
            case MOCK_SELL:
                return Color.web("951821");
            case MOCK_BUY:
                return Color.web("227E14");
            default :
                return Color.BLACK;
                    
        }
        //http://colorschemedesigner.com/csd-3.5/#0p40ZlZtdvyuR
    }

}
