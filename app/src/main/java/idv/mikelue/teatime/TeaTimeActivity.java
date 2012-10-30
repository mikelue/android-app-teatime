package idv.mikelue.teatime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import idv.mikelue.teatime.controller.ObserveTimerServiceController;
import idv.mikelue.teatime.controller.SessionListLoaderController;
import idv.mikelue.teatime.database.TeaTimeDao;
import idv.mikelue.teatime.view.ColorLoader;
import idv.mikelue.teatime.view.FontLoader;

public class TeaTimeActivity extends FragmentActivity {
	/**
	 * The probable number of ticking rounds at the same time.
	 */
	public final static int DEFAULT_CONCURRENT_TICKING_ROUNDS = 3;

	/**
	 * Sets the id of ended round for start this Activity.<p>
	 *
	 * @param intent The intent used to start this Activity
	 * @param idOfEndedRounds The id of ended rounds
	 */
	private final static String ID_OF_ENDED_ROUNDS = "_id_of_ended_rounds_" ;
	public static void putEndedIdOfRounds(Intent intent, int[] idOfEndedRounds)
	{
		intent.putExtra(ID_OF_ENDED_ROUNDS, idOfEndedRounds);
	}

	private final static String TAG = TeaTimeActivity.class.getSimpleName();

	private TeaTimeDao dao;
	private SessionListLoaderController sessionLoaderCtrl;
	private ObserveTimerServiceController timerServiceCtrl;
	private int[] justEndedRoundsIds;

	public TeaTimeActivity()
	{
		Log.d(TAG, "A new instance");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() != R.id.button_setting) {
			return super.onOptionsItemSelected(item);
		}

		Intent intentStartPrefEditor = new Intent(this, SettingActivity.class);
		startActivity(intentStartPrefEditor);

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ColorLoader.initLoader(this, hashCode());

		setContentView(R.layout.main);

		justEndedRoundsIds = getIntent().getIntArrayExtra(ID_OF_ENDED_ROUNDS);

		setupDatabase();
		initServiceController();

		setupWindow();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();

		initServiceController();
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		releaseBoundService();
		releaseDatabase();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		ColorLoader.releaseLoader(hashCode());
		FontLoader.releaseLoader();
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		justEndedRoundsIds = intent.getIntArrayExtra(ID_OF_ENDED_ROUNDS);
	}

	private void setupWindow()
	{
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	}
	private void initServiceController()
	{
		if (timerServiceCtrl != null) {
			return;
		}

		timerServiceCtrl = new ObserveTimerServiceController(
			this,
			new SessionListLoaderController(
				this, dao
			),
			justEndedRoundsIds
		);
		timerServiceCtrl.bindService();

		justEndedRoundsIds = null;
	}
	private void releaseBoundService()
	{
		timerServiceCtrl.unbindService();
		timerServiceCtrl = null;
	}
	private void releaseDatabase()
	{
		dao.release();
	}
	private void setupDatabase()
	{
		dao = new TeaTimeDao(this);
		dao.initDatabase();
	}
}
