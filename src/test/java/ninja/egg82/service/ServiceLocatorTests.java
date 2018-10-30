package ninja.egg82.service;

import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceLocatorTests {
    @Test
    public void testAddsAllValues() {
        ServiceLocator.register(new TestClass());

        Assertions.assertTrue(ServiceLocator.contains(TestClass.class), "SL did not properly register \"TestClass\"");
        Assertions.assertTrue(ServiceLocator.contains(TestSubclass.class), "SL did not properly register TestClass's subclass \"TestSubclass\"");
        Assertions.assertTrue(ServiceLocator.contains(TestInterface.class), "SL did not properly register TestClass's interface \"TestInterface\"");
        Assertions.assertFalse(ServiceLocator.contains(TestSubinterface.class), "SL mistakenly registered TestClass's subclass's interface \"TestSubinterface\"");

        System.out.println("\"register\" correctly registers all subclasses and subinterfaces.");
        System.out.flush();
    }

    @Test
    public void testRemovesAllValues() {
        ServiceLocator.register(new TestClass());
        ServiceLocator.remove(TestInterface.class);

        Assertions.assertFalse(ServiceLocator.contains(TestClass.class), "SL did not properly de-register \"TestClass\" from TestInterface");
        Assertions.assertFalse(ServiceLocator.contains(TestInterface.class), "SL did not properly de-register TestClass's interface \"TestInterface\" from TestInterface");
        Assertions.assertFalse(ServiceLocator.contains(TestSubclass.class), "SL did not properly de-register TestClass's subclass \"TestSubclass\" from TestInterface");

        ServiceLocator.register(new TestClass());
        ServiceLocator.remove(TestSubclass.class);

        Assertions.assertFalse(ServiceLocator.contains(TestClass.class), "SL did not properly de-register \"TestClass\" from TestSubclass");
        Assertions.assertFalse(ServiceLocator.contains(TestInterface.class), "SL did not properly de-register TestClass's interface \"TestInterface\" from TestSubclass");
        Assertions.assertFalse(ServiceLocator.contains(TestSubclass.class), "SL did not properly de-register TestClass's subclass \"TestSubclass\" from TestSubclass");

        System.out.println("\"remove\" correctly de-registers all subclasses and subinterfaces.");
        System.out.flush();
    }

    @Test
    public void testRemoveReturnsExpectedValues() {
        ServiceLocator.register(new TestClass());
        Set<? extends TestInterface> removed = ServiceLocator.remove(TestInterface.class);

        Assertions.assertEquals(1, removed.size(), "SL did not fill the removed result set with the correct number of values.");
        Assertions.assertTrue(removed.toArray()[0].getClass().equals(TestClass.class), "SL did not fill the removed result set with the correct value.");

        System.out.println("\"remove\" returns the correct services.");
        System.out.flush();
    }

    @Test
    public void testGetsCorrectSubclass() throws InstantiationException, IllegalAccessException {
        ServiceLocator.register(new TestClass());

        Assertions.assertTrue(ServiceLocator.getOptional(TestClass.class).isPresent(), "SL did not properly get \"TestClass\"");
        Assertions.assertTrue(ServiceLocator.getOptional(TestSubclass.class).isPresent(), "SL did not properly get TestClass's subclass \"TestSubclass\"");
        Assertions.assertTrue(ServiceLocator.getOptional(TestInterface.class).isPresent(), "SL did not properly get TestClass's interface \"TestInterface\"");
        Assertions.assertFalse(ServiceLocator.getOptional(TestSubinterface.class).isPresent(), "SL mistakenly got TestClass's subclass's interface \"TestSubinterface\"");

        System.out.println("\"get\" correctly returns from subclasses and subinterfaces.");
        System.out.flush();
    }

    class TestClass extends TestSubclass implements TestInterface {}
    interface TestInterface {}
    class TestSubclass {}
    interface TestSubinterface {}
}
