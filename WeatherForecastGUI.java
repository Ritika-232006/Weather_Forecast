import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import org.json.JSONObject;

public class WeatherForecastGUI {
    private static final String API_KEY = "2b48da5719d444c3dbf380fc33381d72";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String AIR_QUALITY_URL = "https://api.openweathermap.org/data/2.5/air_pollution";

    private JFrame frame;
    private JPanel mainPanel;
    private JTextField cityField;
    private JLabel cityLabel, temperatureLabel, descriptionLabel, suggestionLabel, humidityLabel, windLabel, aqiLabel, aqiWarningLabel;
    private JButton toggleUnitButton;
    private boolean isCelsius = true;

    public WeatherForecastGUI() {
        frame = new JFrame("Weather Forecast");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1600,1500);
        frame.setLayout(new BorderLayout());

        // Main Panel      
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setOpaque(false); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Heading Label 
        JLabel headingLabel = new JLabel("Weather Forecast", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 50));
        headingLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = -1;
        gbc.gridwidth = 2;
        mainPanel.add(headingLabel, gbc);

        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.setOpaque(false); 

        JLabel enterCityLabel = new JLabel("Enter City: ");
        enterCityLabel.setFont(new Font("Arial", Font.BOLD, 22));
        cityField = new JTextField(15);
        cityField.setFont(new Font("Arial", Font.PLAIN, 20));
        JButton getWeatherButton = new JButton("Get Weather");
        getWeatherButton.setFont(new Font("Arial", Font.BOLD, 20)); 
        toggleUnitButton = new JButton("Toggle °F/°C");
        toggleUnitButton.setFont(new Font("Arial", Font.BOLD, 20));

        inputPanel.add(enterCityLabel);
        inputPanel.add(cityField);
        inputPanel.add(getWeatherButton);
        inputPanel.add(toggleUnitButton);

        // Add Input Panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        mainPanel.add(inputPanel, gbc);

        // Weather Details
        cityLabel = createLabel("", 22);
        temperatureLabel = createLabel("", 22);
        descriptionLabel = createLabel("", 22);
        suggestionLabel = createLabel("", 22);
        humidityLabel = createLabel("", 22);
        windLabel = createLabel("", 22);
        aqiLabel = createLabel("", 22);
        aqiWarningLabel = createLabel("", 22);

        gbc.gridy = 2;
        mainPanel.add(cityLabel, gbc);

        gbc.gridy = 3;
        mainPanel.add(temperatureLabel, gbc);

        gbc.gridy = 4;
        mainPanel.add(descriptionLabel, gbc);

        gbc.gridy = 5;
        mainPanel.add(humidityLabel, gbc);

        gbc.gridy = 6;
        mainPanel.add(windLabel, gbc);

        gbc.gridy = 7;
        mainPanel.add(suggestionLabel, gbc);

        // AQI Details
        gbc.gridy = 8;
        mainPanel.add(aqiLabel, gbc);

        gbc.gridy = 9;
        mainPanel.add(aqiWarningLabel, gbc);

        // Add Main Panel to Frame
        frame.setContentPane(new JLabel());
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);

        setInitialBackground();

        // Button Listeners
        getWeatherButton.addActionListener(e -> fetchWeatherData(cityField.getText().trim()));
        toggleUnitButton.addActionListener(e -> toggleUnits());

        frame.setVisible(true);
    }

    private JLabel createLabel(String text, int fontSize) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Times New Roman", Font.BOLD, fontSize));
        label.setForeground(Color.BLACK);
        return label;
    }

    private void setInitialBackground() {
        // Set the initial default background
        try {
            String defaultImagePath = "https://img.freepik.com/free-photo/background-blue-sky-with-white-clouds_1048-2877.jpg?ga=GA1.1.1662973033.1734164669&semt=ais_hybrid"; // Default background image
            Image backgroundImage = ImageIO.read(new URL(defaultImagePath));
            backgroundImage = backgroundImage.getScaledInstance(frame.getWidth(), frame.getHeight(), Image.SCALE_SMOOTH);
            frame.setContentPane(new JLabel(new ImageIcon(backgroundImage)));
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error loading default background image.");
            e.printStackTrace();
        }
    }

    private void fetchWeatherData(String city) {
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a city name.");
            return;
        }

        String urlString = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) response.append(inputLine);
            in.close();

            JSONObject weatherData = new JSONObject(response.toString());

            String cityName = weatherData.getString("name");
            double tempCelsius = weatherData.getJSONObject("main").getDouble("temp");
            int humidity = weatherData.getJSONObject("main").getInt("humidity");
            String description = weatherData.getJSONArray("weather").getJSONObject(0).getString("description");
            double windSpeed = weatherData.getJSONObject("wind").getDouble("speed");

            // Get coordinates for AQI data
            double latitude = weatherData.getJSONObject("coord").getDouble("lat");
            double longitude = weatherData.getJSONObject("coord").getDouble("lon");

            String clothingSuggestion = getClothingSuggestion(tempCelsius, description);

            cityLabel.setText("City: " + cityName);
            temperatureLabel.setText(getTemperatureText(tempCelsius));
            descriptionLabel.setText("Description: " + description);
            humidityLabel.setText("Humidity: " + humidity + "%");
            windLabel.setText("Wind: " + windSpeed + " m/s");
            suggestionLabel.setText("Suggestion: " + clothingSuggestion);

            updateBackground(description);

            // Fetch AQI data
            fetchAqiData(latitude, longitude);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error fetching weather data.");
            ex.printStackTrace();
        }
    }

    private void fetchAqiData(double latitude, double longitude) {
        String aqiUrl = AIR_QUALITY_URL + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(aqiUrl).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) response.append(inputLine);
            in.close();

            JSONObject aqiData = new JSONObject(response.toString());
            int aqi = aqiData.getJSONArray("list").getJSONObject(0).getJSONObject("main").getInt("aqi");

            String aqiText = getAqiText(aqi);
            String aqiWarning = getAqiWarning(aqi);

            aqiLabel.setText("AQI(Air Quality Index):" + aqiText);
            aqiWarningLabel.setText("Warning: " + aqiWarning);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error fetching AQI data.");
            ex.printStackTrace();
        }
    }

    private String getAqiText(int aqi) {
        if (aqi == 1) return "Good";
        else if (aqi == 2) return "Fair";
        else if (aqi == 3) return "Moderate";
        else if (aqi == 4) return "Poor";
        else return "Very Poor";
    }

    private String getAqiWarning(int aqi) {
        if (aqi == 1) return "Air quality is good.";
        else if (aqi == 2) return "Air quality is acceptable.";
        else if (aqi == 3) return "Air quality is moderate. Sensitive individuals may experience symptoms.";
        else if (aqi == 4) return "Air quality is poor. Limit prolonged outdoor exertion.";
        else return "Air quality is very poor. Avoid outdoor activities.";
    }

    private String getClothingSuggestion(double temperature, String description) {
        if (description.contains("rain")) return "Carry an umbrella.";
        if (temperature < 10) return "Wear a warm coat.";
        if (temperature < 20) return "Wear a sweater.";
        return "Dress comfortably.";
    }

    private String getTemperatureText(double tempCelsius) {
        if (isCelsius) {
            return String.format("Temperature: %.1f°C", tempCelsius);
        } else {
            double tempFahrenheit = (tempCelsius * 9 / 5) + 32;
            return String.format("Temperature: %.1f°F", tempFahrenheit);
        }
    }

    private void toggleUnits() {
        isCelsius = !isCelsius;
        fetchWeatherData(cityField.getText().trim());
    }

    private void updateBackground(String description) {
        String imagePath = "";
        if (description.contains("clear")) {
            imagePath = "https://media.istockphoto.com/id/1402473970/photo/orange-sky-and-clouds-background-background-of-colorful-sky-concept-amazing-sunset-with.jpg?s=612x612&w=0&k=20&c=IpH3EkDUZy2Mdldagii92jx4FU5B0dTtQOaYpK6byBI=";
        } else if (description.contains("rain")) {
            imagePath = "https://img.freepik.com/free-vector/realistic-clouds-with-falling-rain_1017-33597.jpg";
        } else if (description.contains("snow")) {
            imagePath = "https://img.freepik.com/free-vector/falling-snow-winter-landscape-with-cold-sky-blizzard-snowflakes-snowdrift-realistic-style_333792-42.jpg";
        } else if (description.contains("cloud")) {
            imagePath = "https://clipart-library.com/new_gallery/634032_cloudy-sky-png.png";
        } else {
            imagePath = "https://img.freepik.com/free-vector/realistic-smoke-clouds-background_1017-38295.jpg";
        }

        try {
            Image backgroundImage = ImageIO.read(new URL(imagePath));
            backgroundImage = backgroundImage.getScaledInstance(frame.getWidth(), frame.getHeight(), Image.SCALE_SMOOTH);
            frame.setContentPane(new JLabel(new ImageIcon(backgroundImage)));
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error loading background image.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new WeatherForecastGUI();
    }
}