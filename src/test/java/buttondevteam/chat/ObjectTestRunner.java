package buttondevteam.chat;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Based on {@link Parameterized}
 * 
 * @author NorbiPeti
 *
 */
public class ObjectTestRunner extends Suite {
	/**
	 * Annotation for a method which provides parameters to be injected into the test class constructor by <code>Parameterized</code>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Objects {
	}

	private static class TestClassRunnerForObjects extends BlockJUnit4ClassRunner {

		private List<Object> objectList;
		private int fParameterSetNumber;

		TestClassRunnerForObjects(Class<?> type, List<Object> objectList, int i) throws InitializationError {
			super(type);
			this.objectList = objectList;
			fParameterSetNumber = i;
		}

		@Override
		public Object createTest() throws Exception {
			return objectList.get(fParameterSetNumber);
		}

		@Override
		protected String getName() {
			return String.format("[%s]", fParameterSetNumber);
		}

		@Override
		protected String testName(final FrameworkMethod method) {
			return String.format("%s[%s]", method.getName(), fParameterSetNumber);
		}

		@Override
		protected void validateConstructor(List<Throwable> errors) {
			validateOnlyOneConstructor(errors);
		}

		@Override
		protected Statement classBlock(RunNotifier notifier) {
			return childrenInvoker(notifier);
		}

		@Override
		protected Annotation[] getRunnerAnnotations() {
			return new Annotation[0];
		}
	}

	private final ArrayList<Runner> runners = new ArrayList<>();

	/**
	 * Only called reflectively. Do not use programmatically.
	 */
	public ObjectTestRunner(Class<?> klass) throws Throwable {
		super(klass, Collections.emptyList());
		List<Object> objectsList = getObjectsList(getTestClass());
		for (int i = 0; i < objectsList.size(); i++)
			runners.add(new TestClassRunnerForObjects(getTestClass().getJavaClass(), objectsList, i));
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@SuppressWarnings("unchecked")
	private List<Object> getObjectsList(TestClass klass) throws Throwable {
		return (List<Object>) getObjectsMethod(klass).invokeExplosively(null);
	}

	public static FrameworkMethod getObjectsMethod(TestClass testClass) throws Exception {
		List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Objects.class);
		for (FrameworkMethod each : methods) {
			int modifiers = each.getMethod().getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
				return each;
		}

		throw new Exception("No public static parameters method on class " + testClass.getName());
	}
}
