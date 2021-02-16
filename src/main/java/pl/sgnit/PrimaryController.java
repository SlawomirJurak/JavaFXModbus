package pl.sgnit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;

public class PrimaryController {

    public Circle connectState;
    public TextField ipText;
    public Button connectButton;
    String connectionString = "modbus://localhost";

    public Button primaryButton;
    public Circle pin1;
    public Circle pin3;
    public Circle pin2;
    public Circle pin4;
    public Circle pin5;
    public Circle pin6;
    public Circle pin7;
    public Circle pin8;

    public Circle pin21;
    public Circle pin11;
    public Circle pin31;
    public Circle pin41;
    public Circle pin51;
    public Circle pin61;
    public Circle pin71;
    public Circle pin81;

    public Label messageLabel;

    private Map<String, Circle> circlesRead;
    private Map<String, Circle> circlesWrite;
    private Map<String, Integer> pinNo;

    Color pinOnColor = Color.web("#1fff5e");
    Color pinOffColor = Color.web("#4c514d");

    PlcConnection plcConnection;

    @FXML
    public void initialize() {
        circlesRead = new HashMap<>();
        circlesRead.put("P1", pin1);
        circlesRead.put("P2", pin2);
        circlesRead.put("P3", pin3);
        circlesRead.put("P4", pin4);
        circlesRead.put("P5", pin5);
        circlesRead.put("P6", pin6);
        circlesRead.put("P7", pin7);
        circlesRead.put("P8", pin8);

        circlesWrite = new HashMap<>();
        circlesWrite.put("P1", pin11);
        circlesWrite.put("P2", pin21);
        circlesWrite.put("P3", pin31);
        circlesWrite.put("P4", pin41);
        circlesWrite.put("P5", pin51);
        circlesWrite.put("P6", pin61);
        circlesWrite.put("P7", pin71);
        circlesWrite.put("P8", pin81);

        circlesWrite.forEach((key, circle) -> {
            circle.setUserData(false);
            circle.setOnMouseClicked(this::changeState);
        });

        pinNo = new HashMap<>();
        pinNo.put("P1", 1);
        pinNo.put("P2", 2);
        pinNo.put("P3", 3);
        pinNo.put("P4", 4);
        pinNo.put("P5", 5);
        pinNo.put("P6", 6);
        pinNo.put("P7", 7);
        pinNo.put("P8", 8);

    }

    @FXML
    private void readPinSates() {
        ScheduledExecutorService scheduler
            = Executors.newSingleThreadScheduledExecutor();

        Runnable task = new ReadPinsState();
        int initialDelay = 500;
        int periodicDelay = 500;

        scheduler.scheduleAtFixedRate(task, initialDelay, periodicDelay,
            TimeUnit.MILLISECONDS);
    }

    private void scheduleReadPinState() throws ExecutionException, InterruptedException {
        if (plcConnection == null) {
            return;
        }

        PlcReadRequest.Builder builder = plcConnection.readRequestBuilder();
//        pinNo.forEach((pin, number) -> {
//            builder.addItem(pin, "coil:" + number);
//        });
        builder.addItem("P3", "coil:3");
        builder.addItem("seconds", "holding-register:7");
        builder.addItem("minutes", "holding-register:6");

        PlcReadRequest readRequest = builder.build();

        PlcReadResponse response = readRequest.execute().get();

//        circlesRead.forEach((pin, circle) -> {
//            boolean pinState = (boolean) response.getObject(pin);
//            circle.setFill(pinState ? pinOnColor : pinOffColor);
//        });
        boolean pinState = response.getBoolean("P3");
        Short seconds = response.getShort("seconds");
        Short minutes = response.getShort("minutes");
        Platform.runLater(() -> {
            pin3.setFill(pinState ? pinOnColor : pinOffColor);
            messageLabel.setText(String.format("%02d:%02d", minutes, seconds));
        });
    }

    @FXML
    private void writePinStates() {
        if (plcConnection == null) {
            return;
        }

        if (!plcConnection.getMetadata().canWrite()) {
            System.out.println("This connection doesn't support reading.");
            return;
        }

        PlcWriteRequest.Builder builder = plcConnection.writeRequestBuilder();
        builder.addItem("P8", "coil:9", true);
        builder.addItem("value-3", "holding-register:1", 255);
//        pinNo.forEach((pin, number) -> {
//            builder.addItem(pin, "coil:" + number + 8, (boolean) circlesWrite.get(pin).getUserData());
//        });
        PlcWriteRequest writeRequest = builder.build();

        try {
            PlcWriteResponse response = writeRequest.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void changeState(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Circle circle = (Circle) event.getSource();
            Boolean pinState = !(Boolean) circle.getUserData();
            circle.setUserData(pinState);

            circle.setFill(pinState ? pinOnColor : pinOffColor);
        }
    }

    private String createConnectionString() {
        return "modbus:tcp://" + ipText.getText() + ":502";
    }

    @FXML
    private void connectToPLC() {
        try {
            plcConnection = new PlcDriverManager().getConnection(createConnectionString());
            if (plcConnection.isConnected()) {
                connectState.setFill(pinOnColor);
            }
        } catch (PlcConnectionException e) {
            e.printStackTrace();
        }
    }

    class ReadPinsState implements Runnable {

        @Override
        public void run() {
            try {
                scheduleReadPinState();
            } catch (ExecutionException | InterruptedException ex) {
                Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
