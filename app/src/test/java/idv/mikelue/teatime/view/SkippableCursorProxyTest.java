package idv.mikelue.teatime.view;

import android.database.Cursor;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SkippableCursorProxyTest extends idv.mikelue.teatime.test.AbstractMockAndroidEnvTestBase {
	public SkippableCursorProxyTest() {}

	/**
	 * Tests the skipping of row(the count of {@link Cursor} should be changed).<p>
	 */
	@Test(dataProvider="SkipRow")
	public void skipRow(
		int[] testRows, int[] skippedIds, int exptecedNumberOfCursor
	) {
		SkippableCursorProxy testProxy = new SkippableCursorProxy(
			"<test_column_name>"
		);
		testProxy.setCursor(new MockCursor(testRows).getMockInstance());

		/**
		 * Setup the skipping
		 */
		for (int skippedId: skippedIds) {
			testProxy.skipRow(skippedId);
		}
		// :~)

		Assert.assertEquals(
			testProxy.getCountOfCursor(), exptecedNumberOfCursor
		);
	}
	@DataProvider(name="SkipRow")
	private Object[][] getSkipRow()
	{
		return new Object[][] {
			{ new int[] { 1 }, new int[] { 1 }, 0 },
			{ new int[] { 1, 2, 3 }, new int[] { 2 }, 2 },
			{ new int[] { 1, 2, 3 }, new int[] { 3 }, 2 },
			{ new int[] { 1, 2, 3 }, new int[] { 1, 2, 3 }, 0 },
			{ new int[] { 1, 2, 3 }, new int[0], 3 },
			{ new int[0], new int[0], 0 } // Empty data
		};
	}

	/**
	 * Tests the removal for skipping of row(the count of {@link Cursor} should be changed).<p>
	 */
	@Test(dataProvider="RemoveSkip")
	public void removeSkip(
		int[] testRows, int[] removalOfSkippedIds, int exptecedNumberOfCursor
	) {
		SkippableCursorProxy testProxy = new SkippableCursorProxy(
			"<test_column_name>"
		);
		testProxy.setCursor(new MockCursor(testRows).getMockInstance());

		/**
		 * Setup the removal of skipping for all of the rows
		 */
		for (int skippedId: testRows) {
			testProxy.skipRow(skippedId);
		}
		for (int removalOfSkippedId: removalOfSkippedIds) {
			testProxy.removeSkip(removalOfSkippedId);
		}
		// :~)

		Assert.assertEquals(
			testProxy.getCountOfCursor(), exptecedNumberOfCursor
		);
	}
	@DataProvider(name="RemoveSkip")
	private Object[][] getRemoveSkip()
	{
		return new Object[][] {
			{ new int[] { 1 }, new int[] { 1 }, 1 },
			{ new int[] { 1, 2, 3 }, new int[] { 2 }, 1 },
			{ new int[] { 1, 2, 3 }, new int[] { 3 }, 1 },
			{ new int[] { 1, 2, 3 }, new int[] { 1, 2, 3 }, 3 },
			{ new int[] { 1, 2, 3 }, new int[0], 0 },
			{ new int[0], new int[0], 0 } // Empty data
		};
	}

	/**
	 * Test the moving of cursor for next non-skipped row.<p>
	 *
	 * This method doesn't test the empty of data because it is impossible that
	 * {@link SkippableCursorProxy#moveCursorToNonSkippedPosition} is called
	 * while the {@link Cursor} contains no data.<p>
	 */
	@Test(dependsOnMethods={"skipRow", "removeSkip"}, dataProvider="MoveCursorToNonSkippedPosition")
	public void moveCursorToNonSkippedPosition(
		int[] testRows, int[] skippedIds,
		int[][] moveAndExpectedCursorPositions
	) {
		Cursor mockCursor = new MockCursor(testRows).getMockInstance();
		SkippableCursorProxy testProxy = new SkippableCursorProxy(
			"<test_column_name>"
		);
		testProxy.setCursor(mockCursor);

		/**
		 * Setup the skipping of rows
		 */
		if (skippedIds != null) {
			for (int skipId: skippedIds) {
				testProxy.skipRow(skipId);
			}
		}
		// :~)

		for (int i = 0; i < moveAndExpectedCursorPositions.length; i++) {
			mockCursor.moveToPosition(moveAndExpectedCursorPositions[i][0]);
			testProxy.moveCursorToNonSkippedPosition();

			Assert.assertEquals(
				mockCursor.getPosition(), moveAndExpectedCursorPositions[i][1]
			);
		}
	}
	@DataProvider(name="MoveCursorToNonSkippedPosition")
	private Object[][] getMoveCursorToNonSkippedPosition()
	{
		return new Object[][] {
			/**
			 * Nothing skipped
			 */
			{ new int[] { 24, 25, 26, 27 }, new int[0],
				new int[][] {
					new int[] { 0, 0 },
					new int[] { 1, 1 },
					new int[] { 2, 2 },
					new int[] { 3, 3 },
				}
			},
			// :~)
			/**
			 * Skiped one row
			 */
			{ new int[] { 24, 25, 26, 27 }, new int[] { 24 },
				new int[][] {
					new int[] { 0, 1 },
					new int[] { 1, 2 },
					new int[] { 2, 3 },
				}
			},
			{ new int[] { 24, 25, 26, 27 }, new int[] { 27 },
				new int[][] {
					new int[] { 0, 0 },
					new int[] { 1, 1 },
					new int[] { 2, 2 },
				}
			},
			// :~)
			/**
			 * Skiped multiple sequential rows(preceding rows are skipped, 2 non-skipped rows)
			 */
			{ new int[] { 24, 25, 26, 27 }, new int[] { 24, 25 },
				new int[][] {
					new int[] { 0, 2 },
					new int[] { 1, 3 },
				}
			},
			// :~)
			/**
			 * Skiped multiple sequential rows(following rows are skipped, 2 non-skipped rows)
			 */
			{ new int[] { 24, 25, 26, 27 }, new int[] { 26, 27 },
				new int[][] {
					new int[] { 0, 0 },
					new int[] { 1, 1 },
				}
			},
			// :~)
			/**
			 * Skiped multiple sequential rows(intercepting, 2 non-skipped rows)
			 */
			{ new int[] { 24, 25, 26, 27 }, new int[] { 25, 26 },
				new int[][] {
					new int[] { 0, 0 },
					new int[] { 1, 3 },
				}
			},
			// :~)
			/**
			 * Skiped multiple interrmittent rows(3 non-skipped rows)
			 */
			{ new int[] { 12, 14, 15, 16, 17, 18, 19 }, new int[] { 12, 14, 17, 18 },
				new int[][] {
					new int[] { 0, 2 },
					new int[] { 1, 3 },
					new int[] { 2, 6 },
				}
			},
			// :~)
		};
	}

	/**
	 * Test the resetting of proxy(The cursor is reloaded and begins from first
	 * row).<p>
	 */
	@Test(dependsOnMethods="moveCursorToNonSkippedPosition")
	public void reset()
	{
		Cursor testCursor = new MockCursor(
			new int[] { 50, 60, 70, 80, 90 }
		).getMockInstance();

		SkippableCursorProxy testProxy = new SkippableCursorProxy(
			"<test_column_name>"
		);
		testProxy.setCursor(testCursor);

		/**
		 * Setup the skipping of rows
		 */
		for (int skipId: new int[] { 50, 70, 90 }) {
			testProxy.skipRow(skipId);
		}
		// :~)

		/**
		 * Perform the iterator(moving) for all non-skipped rows
		 */
		for (int i = 0; i < testProxy.getCountOfCursor(); i++) {
			testCursor.moveToPosition(i);
			testProxy.moveCursorToNonSkippedPosition();
		}
		// :~)

		testProxy.reset();

		/**
		 * Perform the iterator(moving) for all non-skipped rows;
		 * if the resetting failed, there should be an "overflow" exception
		 */
		for (int i = 0; i < testProxy.getCountOfCursor(); i++) {
			testCursor.moveToPosition(i);
			testProxy.moveCursorToNonSkippedPosition();
		}
		// :~)
	}
}
