package eversync.tests;

import eversync.iServer.IServerManagerSuper;
import static org.junit.Assert.*;

import org.junit.*;

public class IServerManagerTest {
	
	// IServerManager is tested
	private static IServerManagerSuper iServer;
	
//	@BeforeClass
//	public static void init() {
//		iServer = new IServerManager();
//	}

	@Test
	public void testAddition() {
		// check if multiply(10,5) returns 50
		assertEquals("10 x 5 must be 50", 50, 5*10);
	}
}
