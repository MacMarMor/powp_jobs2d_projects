package edu.kis.powp.jobs2d;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.kis.legacy.drawer.panel.DrawPanelController;
import edu.kis.legacy.drawer.shape.LineFactory;
import edu.kis.powp.appbase.Application;
import edu.kis.powp.jobs2d.command.gui.*;
import edu.kis.powp.jobs2d.drivers.decorator.DeviceUsageDecorator;
import edu.kis.powp.jobs2d.drivers.SelectMouseFigureOptionListener;
import edu.kis.powp.jobs2d.drivers.adapter.LineDriverAdapter;
import edu.kis.powp.jobs2d.drivers.composite.DriverComposite;
import edu.kis.powp.jobs2d.events.*;
import edu.kis.powp.jobs2d.features.*;

public class TestJobs2dApp {
	private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	/**
	 * Setup test concerning preset figures in context.
	 * 
	 * @param application Application context.
	 */
	private static void setupPresetTests(Application application) {
		SelectTestFigureOptionListener selectTestFigureOptionListener = new SelectTestFigureOptionListener(
				DriverFeature.getDriverManager());
		SelectTestFigure2OptionListener selectTestFigure2OptionListener = new SelectTestFigure2OptionListener(
				DriverFeature.getDriverManager());

		application.addTest("Figure Joe 1", selectTestFigureOptionListener);
		application.addTest("Figure Joe 2", selectTestFigure2OptionListener);
	}

	/**
	 * Setup test using driver commands in context.
	 * 
	 * @param application Application context.
	 */
	private static void setupCommandTests(Application application) {
		application.addTest("Load secret command", new SelectLoadSecretCommandOptionListener());

		application.addTest("Run command", new SelectRunCurrentCommandOptionListener(DriverFeature.getDriverManager()));

		application.addTest("Mouse figure", new SelectMouseFigureOptionListener(application.getFreePanel(), DriverFeature.getDriverManager()));

		application.addTest("Count subcommands", new SelectCommandVisitorCounterListener(DriverFeature.getDriverManager()));

		application.addTest("ICompoundCommandVisitorTest", new SelectICompoundCommandVisitorCounterListener());

		application.addTest("TestImmutableCommandBuilder", new SelectTestImmutableCommandBuilderTest());
	}

	/**
	 * Setup driver manager, and set default Job2dDriver for application.
	 * 
	 * @param application Application context.
	 */
	private static void setupDrivers(Application application) {
		DriverComposite driverComposite = new DriverComposite();
		DriverFeature driverFeature = new DriverFeature(application);
		Job2dDriver loggerDriver = new LoggerDriver();
		driverFeature.addDriver("Logger driver", loggerDriver);

		DrawPanelController drawerController = DrawerFeature.getDrawerController();
		Job2dDriver driver = new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic");
		driverFeature.addDriver("Line Simulator", driver);
		DriverFeature.getDriverManager().setCurrentDriver(driver);

		driver = new LineDriverAdapter(drawerController, LineFactory.getSpecialLine(), "special");
		driverFeature.addDriver("Special line Simulator", driver);

		driverComposite.add(new LoggerDriver());
		driverComposite.add(new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic"));
		driverComposite.add(new LineDriverAdapter(drawerController, LineFactory.getSpecialLine(), "special"));
		driverFeature.addDriver("Driver composite", driverComposite);

		driver = new DeviceUsageDecorator(new LineDriverAdapter(drawerController, LineFactory.getBasicLine(), "basic"), DeviceUsageFeature.getDeviceUsageManager());
		driverFeature.addDriver("Line Simulator with Device Usage", driver);

	}

	private static void setupWindows(Application application) {

		CommandManagerWindow commandManager = new CommandManagerWindow(CommandsFeature.getDriverCommandManager());
		application.addWindowComponent("Command Manager", commandManager);

		CommandManagerWindowCommandChangeObserver windowObserver = new CommandManagerWindowCommandChangeObserver(
				commandManager);
		CommandsFeature.getDriverCommandManager().getChangePublisher().addSubscriber(windowObserver);

		DeviceUsageCalculatorWindow deviceUsageCalculatorWindow = new DeviceUsageCalculatorWindow(DeviceUsageFeature.getDeviceUsageManager());
		application.addWindowComponent("Device Usage Manager", deviceUsageCalculatorWindow);

		DeviceUsageCalculatorWindowDistanceChangeObserver deviceUsageWindowObserver =
				new DeviceUsageCalculatorWindowDistanceChangeObserver(deviceUsageCalculatorWindow);
		DeviceUsageFeature.getDeviceUsageManager().getPublisher().addSubscriber(deviceUsageWindowObserver);

		ComplexCommandEditorWindow complexCommandEditorWindow = new ComplexCommandEditorWindow();
		ComplexCommandWindowCommandChangeObserver complexCommandWindowCommandChangeObserver = new ComplexCommandWindowCommandChangeObserver(complexCommandEditorWindow);
		application.addWindowComponent("Complex command editor", complexCommandEditorWindow);
		CommandsFeature.getDriverCommandManager().getChangePublisher().addSubscriber(complexCommandWindowCommandChangeObserver);
    
		TransformationMangerWindow transformationManger = new TransformationMangerWindow();
		application.addWindowComponent("Transformation manager", transformationManger);
	}

	/**
	 * Setup menu for adjusting logging settings.
	 * 
	 * @param application Application context.
	 */
	private static void setupLogger(Application application) {

		application.addComponentMenu(Logger.class, "Logger", 0);
		application.addComponentMenuElement(Logger.class, "Clear log",
				(ActionEvent e) -> application.flushLoggerOutput());
		application.addComponentMenuElement(Logger.class, "Fine level", (ActionEvent e) -> logger.setLevel(Level.FINE));
		application.addComponentMenuElement(Logger.class, "Info level", (ActionEvent e) -> logger.setLevel(Level.INFO));
		application.addComponentMenuElement(Logger.class, "Warning level",
				(ActionEvent e) -> logger.setLevel(Level.WARNING));
		application.addComponentMenuElement(Logger.class, "Severe level",
				(ActionEvent e) -> logger.setLevel(Level.SEVERE));
		application.addComponentMenuElement(Logger.class, "OFF logging", (ActionEvent e) -> logger.setLevel(Level.OFF));
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Application app = new Application("Jobs 2D");

				FeatureManager.addFeature(new DrawerFeature(app));
				FeatureManager.addFeature(new CommandsFeature());
				FeatureManager.addFeature(new DeviceUsageFeature());
				FeatureManager.addFeature(new DriverFeature(app));
				FeatureManager.addFeature(new RecordingFeature(app,DriverFeature.getDriverManager()));
				FeatureManager.addFeature(new CommandHistoryFeature(app));
				DriverFeature.setUpDriverNameLabelChangeManager();
				FeatureManager.setupFeatures();

				setupDrivers(app);
				setupPresetTests(app);
				setupCommandTests(app);
				setupLogger(app);
				setupWindows(app);

				app.setVisibility(true);
			}
		});
	}

}
