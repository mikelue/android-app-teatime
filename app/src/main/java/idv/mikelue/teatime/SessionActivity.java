package idv.mikelue.teatime;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import idv.mikelue.teatime.controller.RoundEditorController;
import idv.mikelue.teatime.controller.SessionEditorController;
import idv.mikelue.teatime.database.StopgapRoundDao;
import idv.mikelue.teatime.database.TeaTimeDao;
import idv.mikelue.teatime.model.Session.IconType;
import idv.mikelue.teatime.model.Session;
import idv.mikelue.teatime.view.ColorLoader;
import idv.mikelue.teatime.view.FontLoader;
import idv.mikelue.teatime.view.RoundCursorAdapter;

/**
 * This activity is the host to add new or to edit the timers in session.<p>
 */
public class SessionActivity extends FragmentActivity {
	public final static int NEW_SESSION_ID = -2;

	final static String INTENT_SESSION_ID = "_session_id_";
	final static String INTENT_SESSION_NAME = "_session_name_";
	final static String INTENT_SESSION_COLOR_VALUE = "_session_color_value_";

	private Session currentSession;

	private TeaTimeDao persistDao;
	private StopgapRoundDao stopgapDao;
	private RoundEditorController roundCtrl;

	private View buttonOfCancel;

	/**
	 * Sets the session id of intent.<p>
	 *
	 * @param intent The intent used to start this activity
	 * @param sessionData The data of session
	 */
	public static void setSessionData(Intent intent, Session sessionData)
	{
		intent.putExtra(INTENT_SESSION_ID, sessionData.getId());
		intent.putExtra(INTENT_SESSION_NAME, sessionData.getName());
		intent.putExtra(INTENT_SESSION_COLOR_VALUE, sessionData.getIconType().getDatabaseValue());
	}

	private final static String TAG = SessionActivity.class.getSimpleName();

	public SessionActivity() {}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ColorLoader.initLoader(this, hashCode());

		setContentView(R.layout.session);
		buttonOfCancel = findViewById(R.id.button_cancel);

		setupSession();
		setupDatabase();
		setupRoundController();
		setupSessionController();
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();

		ColorLoader.initLoader(this, hashCode());
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		roundCtrl.release();
		releaseDatabase();

		ColorLoader.releaseLoader(hashCode());
		FontLoader.releaseLoader();
	}

	@Override
	public void onBackPressed()
	{
		buttonOfCancel.performClick();
	}

	/**
	 * Setups the target session of current activity
	 */
	private void setupSession()
	{
		Intent intent = this.getIntent();

		currentSession = new Session(
			intent.getIntExtra(INTENT_SESSION_ID, NEW_SESSION_ID)
		);

		/**
		 * New session
		 */
		if (intent.getAction().equals(Intent.ACTION_INSERT)) {
			currentSession.setName(
				getResources().getString(R.string.default_session_name)
			);
			currentSession.setIconType(
				IconType.valueOfDatabaseValue(new Random().nextInt(8) + 1)
			);

			return;
		}
		// :~)

		/**
		 * Edit session
		 */
		currentSession.setName(
			intent.getStringExtra(INTENT_SESSION_NAME)
		);
		currentSession.setIconType(
			IconType.valueOfDatabaseValue(intent.getIntExtra(INTENT_SESSION_COLOR_VALUE, -1))
		);
		// :~)
	}

	private void setupSessionController()
	{
		new SessionEditorController(
			this, currentSession,
			persistDao, roundCtrl
		);
	}
	private void setupRoundController()
	{
		/**
		 * Setup the controller for rounds
		 */
		roundCtrl = new RoundEditorController(
			this, currentSession,
			persistDao, stopgapDao
		);
		roundCtrl.init();
		// :~)
	}
	private void setupDatabase()
	{
		persistDao = new TeaTimeDao(this);
		stopgapDao = new StopgapRoundDao(this);
	}
	private void releaseDatabase()
	{
		stopgapDao.release();
		persistDao.release();
	}
}
