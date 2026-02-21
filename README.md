# Building an Android Sensor Reader App


---

## Why I Wrote This

A few months ago, I was building a small fitness tracking feature for an Android app. I needed to read accelerometer data — seemed simple enough. But I quickly ran into a wall. The official documentation was scattered, Stack Overflow answers were outdated, and most tutorials either skipped the "why" completely or dumped a wall of code with zero explanation.

So I went through the trial and error myself, figured it out, and now I'm writing the guide I wish existed back then.

By the end of this article, you'll have a working Android app that reads live data from multiple sensors — accelerometer, gyroscope, light, pressure, temperature, and more. More importantly, you'll understand *why* the code is written the way it is, not just *what* to copy-paste.

Let's get into it.

---

## What You Need Before Starting

Here's what you need set up on your machine:

- **Android Studio** — download it free from [developer.android.com/studio](https://developer.android.com/studio). When it launches for the first time, just let it finish downloading the SDK. It takes 10–15 minutes but it's a one-time thing.
- **Java** — already bundled inside Android Studio, no separate download needed.
- **An Android phone** — plug it in via USB for testing. You can use the emulator too, but a real phone gives you actual live sensor readings, which is much more satisfying.

If you're on a low-spec machine, you can use GitHub Codespaces and write everything in a browser tab without installing anything heavy locally. The build still works the same way.

---

## First, Let's Understand What Sensors Actually Are

Before writing a single line, it helps to know what we're dealing with.

Your Android phone has tiny hardware chips that measure the physical world around it. Android groups these into three categories:

**Motion Sensors** measure movement. The accelerometer measures forces along the X, Y, and Z axes — when you shake your phone, the accelerometer feels it. Step counters, screen rotation, and shake-to-undo features all use this. The gyroscope measures how fast the phone is *rotating* — games that tilt to steer use this one.

**Environmental Sensors** measure the world around the device. The light sensor tells the phone how bright the room is (that's how auto-brightness works). The pressure sensor measures atmospheric pressure, which weather apps use. Temperature and humidity sensors are rarer but exist on some devices.

**Position Sensors** track where the device is pointing or how close something is to it. The magnetic field sensor is basically a digital compass. The proximity sensor is why your screen goes dark when you hold the phone to your ear during a call.

All of these — regardless of type — are accessed through one single Android API: `SensorManager`. That's your entry point to everything.

---

## Project Structure — Getting Organized First

I want to be clear about something before we start coding: the way you organize your files matters. Not just for passing a code review, but for your own sanity when you come back to this project weeks later.

Here's the structure we'll use:

```
app/src/main/
├── java/com/samsung/sensorreader/
│   ├── model/
│   │   └── SensorItem.java           ← One sensor's data (name, value, unit)
│   ├── manager/
│   │   └── DeviceSensorManager.java  ← All sensor logic lives here
│   ├── adapter/
│   │   └── SensorAdapter.java        ← How the list displays on screen
│   └── MainActivity.java             ← Ties everything together
└── res/
    ├── layout/
    │   ├── activity_main.xml          ← Main screen UI
    │   └── item_sensor.xml            ← One sensor row UI
    └── drawable/                      ← Visual styles
```

Each file has exactly one job. The model doesn't know about the UI. The manager doesn't know how the list is displayed. MainActivity doesn't contain sensor logic. This is called **separation of concerns**, and it's what separates code that can be reviewed and maintained from code that becomes a mess after two weeks.

---

## Step 1 — The Data Model

Create a `model` package inside your main Java folder. Inside it, create `SensorItem.java`.

This class is a simple data container — think of it like one row in a spreadsheet. It holds the sensor's name, what category it belongs to, its current reading, its unit, and whether this particular device even has that sensor.

```java
public class SensorItem {

    private String sensorName;    // "Accelerometer"
    private String sensorType;    // "Motion"
    private String currentValue;  
    private String unit;          // "m/s²"
    private boolean isAvailable;  // true if this phone has this sensor

    public SensorItem(String sensorName, String sensorType,
                      String unit, boolean isAvailable) {
        this.sensorName   = sensorName;
        this.sensorType   = sensorType;
        this.unit         = unit;
        this.isAvailable  = isAvailable;
        this.currentValue = isAvailable ? "Reading..." : "Not Available";
    }

}
```

Why a separate model class instead of just using raw strings? Because if you later want to add a "last updated" timestamp or a history graph, you add it in one place. If you had scattered sensor data all over your activity, you'd be hunting down every place that needs changing.

---

## Step 2 — The Sensor Manager

This is the most important file in the project. Create a `manager` package and inside it, `DeviceSensorManager.java`.

The idea is straightforward: this class owns everything related to sensors. Registering them, receiving their data, and notifying the rest of the app when values change. No other class needs to know how sensors work internally.

### Defining Which Sensors We Want

I define target sensors as a configuration array at the top of the class. Adding or removing a sensor later becomes a one-line change:

```java
public static final SensorConfig[] TARGET_SENSORS = {
    new SensorConfig(Sensor.TYPE_ACCELEROMETER,       "Accelerometer",       "m/s²"),
    new SensorConfig(Sensor.TYPE_GYROSCOPE,           "Gyroscope",           "rad/s"),
    new SensorConfig(Sensor.TYPE_MAGNETIC_FIELD,      "Magnetic Field",      "μT"),
    new SensorConfig(Sensor.TYPE_LIGHT,               "Light",               "lux"),
    new SensorConfig(Sensor.TYPE_PRESSURE,            "Pressure",            "hPa"),
    new SensorConfig(Sensor.TYPE_PROXIMITY,           "Proximity",           "cm"),
    new SensorConfig(Sensor.TYPE_AMBIENT_TEMPERATURE, "Ambient Temperature", "°C"),
    new SensorConfig(Sensor.TYPE_RELATIVE_HUMIDITY,   "Relative Humidity",   "%"),
};
```

`SensorConfig` is a small inner class with three fields: the Android sensor type constant, a display name, and the unit of measurement.

### Registering Sensors to Start Receiving Data

When the app becomes visible to the user, we register each sensor:

```java
public void startListening() {
    for (SensorConfig config : TARGET_SENSORS) {
        Sensor sensor = androidSensorManager.getDefaultSensor(config.type);
        if (sensor != null) {
            androidSensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }
}
```

Two things to note here. First, `getDefaultSensor()` returns `null` if the device doesn't have that sensor — we check before registering because not every Android phone has a barometer or temperature sensor. Second, `SENSOR_DELAY_NORMAL` gives roughly 5 updates per second — fast enough to feel live, slow enough to not hammer the CPU.

### The Most Common Mistake — Forgetting to Unregister

I have seen this bug in production apps more than once. When you register a sensor listener, Android keeps sending data forever — even after the user closes your app — unless you explicitly unregister. This drains the battery silently in the background.

The fix is one line, but you have to actually remember to call it:

```java
public void stopListening() {
    androidSensorManager.unregisterListener(this);
}
```

The rule is: **register in `onResume()`, unregister in `onPause()`**. Every time, no exceptions.

### Receiving Sensor Data

When any registered sensor has a new reading, Android automatically calls this method on your class:

```java
@Override
public void onSensorChanged(SensorEvent event) {
    if (dataListener != null) {
        dataListener.onSensorDataUpdated(event.sensor.getType(), event.values);
    }
}
```

`event.sensor.getType()` tells us which sensor fired. `event.values` is a float array — for single-axis sensors like light, only `values[0]` matters. For three-axis sensors like the accelerometer, `values[0]` is X, `values[1]` is Y, `values[2]` is Z.

We pass this data out through an interface called `OnSensorDataListener`. The manager doesn't know or care what happens to the data after this — it just fires the callback. This is the **Observer pattern**, and it's what keeps the sensor logic cleanly separated from the UI layer.

---

## Step 3 — Displaying the Data

Create an `adapter` package and inside it, `SensorAdapter.java`.

Android uses `RecyclerView` to display scrollable lists. It works like a window that only creates views for what's currently on screen — as you scroll, it reuses ("recycles") old views rather than creating new ones. That's why it stays smooth even with long lists.

The key method in the adapter fills each row with data:

```java
@Override
public void onBindViewHolder(SensorViewHolder holder, int position) {
    SensorItem item = sensorItems.get(position);

    holder.tvSensorName.setText(item.getSensorName());
    holder.tvSensorValue.setText(item.getCurrentValue());
    holder.tvSensorUnit.setText(item.getUnit());

    // Green dot = sensor available, Red dot = not available on this device
    if (item.isAvailable()) {
        holder.statusDot.setBackgroundResource(R.drawable.bg_dot_green);
    } else {
        holder.statusDot.setBackgroundResource(R.drawable.bg_dot_red);
        holder.tvSensorValue.setText("Not available on this device");
    }
}
```

For live updates, we update only the specific row that changed — not the whole list:

```java
public void updateSensorValue(int position, String newValue) {
    sensorItems.get(position).setCurrentValue(newValue);
    notifyItemChanged(position);
}
```

This detail matters. Sensors fire many times per second. If you called `notifyDataSetChanged()` — which redraws every single row — on every sensor event, you'd get constant flickering. Targeting only the one changed row keeps the UI smooth.

---

## Step 4 — MainActivity: Where Everything Connects

MainActivity's job is coordination, not logic. It builds the list, connects the sensor manager to the adapter, and handles the Android Activity lifecycle.

```java
public class MainActivity extends AppCompatActivity
        implements DeviceSensorManager.OnSensorDataListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = new DeviceSensorManager(this);
        sensorManager.setOnSensorDataListener(this);

        buildSensorList();   // creates SensorItem list from TARGET_SENSORS
        setupRecyclerView(); // connects adapter to RecyclerView
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.startListening(); // sensors ON when app is on screen
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.stopListening(); // sensors OFF when app goes to background
    }

    @Override
    public void onSensorDataUpdated(int sensorType, float[] values) {
        Integer index = sensorTypeToIndexMap.get(sensorType);
        if (index == null) return;

        String formatted = DeviceSensorManager.formatValues(sensorType, values);

        runOnUiThread(() -> sensorAdapter.updateSensorValue(index, formatted));
    }
}
```

One line here deserves attention: `runOnUiThread(...)`.

Sensor callbacks arrive on a background thread. Android's rule is strict — only the main UI thread is allowed to update views on screen. If you try to change a TextView's text from a background thread, your app crashes with a `CalledFromWrongThreadException`. The `runOnUiThread()` call moves that update safely to the right thread. This is not optional.

---

## Running the App — What You'll See

Build the project and install it on your phone. When the app opens, you'll see a scrollable list of sensors. The ones your device supports will show a green dot and start displaying live values immediately. The ones it doesn't have will show a red dot.

Try tilting your phone — the accelerometer X, Y, Z values change. Cover the light sensor with your finger — the lux value drops toward zero. That's your code responding to the real physical world in real time.

---

## Quick Concept Reference

| Concept | What it means in practice |
|---|---|
| `SensorManager` | Android system service — your gateway to all sensors |
| `SensorEventListener` | The interface you implement to receive sensor data |
| `onSensorChanged()` | Fires automatically every time a sensor has new data |
| `SENSOR_DELAY_NORMAL` | About 5 updates/sec — good balance of responsiveness and battery |
| `unregisterListener()` | Must call in `onPause()` — forgetting this drains the battery |
| `runOnUiThread()` | Moves UI updates from background thread to main thread safely |
| `notifyItemChanged()` | Updates one RecyclerView row without redrawing the whole list |

---

## What You Can Build Next

Once you can read raw sensor data, a lot of useful things become possible. The accelerometer alone can power a step counter, a fall detector, or a tilt-based game controller. Combining the magnetic field sensor with the accelerometer gives you a working compass. Log pressure readings over time and you have a simple weather trend tracker.

The full source code is in the GitHub repository linked at the bottom of this article. Clone it, run it on your phone, and then try modifying it — add a new sensor, change the update frequency, log values to a file. Breaking things and fixing them is genuinely the fastest way to learn.

If something isn't working, the first place to look is always the sensor lifecycle. Register in `onResume`, unregister in `onPause`. Get that right and most other issues are straightforward to solve.

Good luck, and happy building.
