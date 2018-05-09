package com.jimmy.minesweep;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity {
	private int time = 0;
	private final static int MINES = 15;
	private static int MAP_SIZE = 10;
	private LinearLayout gameViewLayout;
	private Context context;
	private TextView tv_time, tv_mines;
	private boolean isGameStart = false;
	private Button btn_start;
	private TimeThread timeThread;
	private int mineViewWidth;
	private int[][] mineMap;
	private int[][] mineMapOpened;
	private int mineClear;
	private List<Integer> mineIds = new ArrayList<>();
	private List<Integer> mineClearId = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		context = this;
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();
		mineViewWidth = (width - 150) / MAP_SIZE;
		timeThread = new TimeThread();
		initView(context);
	}

	private void initView(Context context) {
		gameViewLayout = (LinearLayout) findViewById(R.id.view_game);
		tv_time = (TextView) findViewById(R.id.tv_time);
		tv_mines = (TextView) findViewById(R.id.tv_mines);

		btn_start = (Button) findViewById(R.id.btn_start);
		btn_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isGameStart) {
					isGameStart = true;
					timeThread = new TimeThread();
					timeThread.start();
					btn_start.setText("����");
				} else {
					isGameStart = false;
					initGame();
					initGameView();
				}

			}
		});
		initGameView();
		initGame();
	}

	private void initGameView() {
		btn_start.setText("��ʼ");
		tv_time.setText("ʱ�䣺0��");
		tv_mines.setText("���ף�15��");
		gameViewLayout.removeAllViews();
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		for (int i = 0; i < MAP_SIZE; i++) {
			LinearLayout linearLayout = new LinearLayout(this);
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			linearLayout.setLayoutParams(params);
			addView(linearLayout, i);
			gameViewLayout.addView(linearLayout);
		}
	}

	// ʣ��δ����ͼ����
	private int mapUnOpenLeft;

	private void initGame() {
		time = 0;
		mineClear = 0;
		mapUnOpenLeft = MAP_SIZE * MAP_SIZE;
		mineMap = new int[MAP_SIZE][MAP_SIZE];
		mineMapOpened = new int[MAP_SIZE][MAP_SIZE];
		// ��ʼ��������
		int mineLeft = MINES;
		int mapLeft = MAP_SIZE * MAP_SIZE;
		int id = 0;
		mineIds.removeAll(mineIds);
		mineClearId.removeAll(mineClearId);
		for (int i = 0; i < MAP_SIZE; i++) {
			for (int j = 0; j < MAP_SIZE; j++) {

				int isMine = 0;

				if (mapLeft == mineLeft) {
					isMine = 1;
					mineLeft--;
				} else {
					if (mineLeft != 0) {
						Random random = new Random();
						int r = random.nextInt(MAP_SIZE * MAP_SIZE);
						// System.out.println("�������" + r);
						if (r < MINES) {
							isMine = 1;
						}
						if (isMine == 1) {
							mineLeft--;
						}
					}
				}
				mineMapOpened[i][j] = 0;
				mineMap[i][j] = isMine;
				mapLeft--;
				System.out.print(isMine);
				if (isMine == 1) {
					mineIds.add(id);
				}
				id++;
			}
			System.out.print("\n");
		}
	}

	// TODO ��ӵ��׿ؼ�
	private void addView(final LinearLayout linearLayout, int count) {
		for (int i = 0; i < MAP_SIZE; i++) {
			final TextView showText = new TextView(this);
			showText.setTextColor(Color.GREEN);
			// showText.setTextSize(15);
			showText.setId(MAP_SIZE * count + i);// ���� id
			showText.setText("*");
			showText.setBackgroundColor(Color.GRAY);
			// set �ı���С
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					mineViewWidth, mineViewWidth);
			// set ���ܾ���
			params.setMargins(1, 1, 1, 1);
			showText.setLayoutParams(params);
			showText.setGravity(Gravity.CENTER);
			showText.setOnClickListener(new MineOnclickListener());
			showText.setOnLongClickListener(new MineOnLongClickListener());
			// ����ı���������
			linearLayout.addView(showText);
		}

	}

	// TODO �򿪸���
	private void openMap(int id) {
		// ���Ϊ��ɨ�����ײ���
		if (isIdClear(id)) {
			return;
		}
		int row = id / MAP_SIZE;
		int column = id % MAP_SIZE;
		System.out.println("�򿪵�" + row + "�е�" + column + "�С���" + id
				+ "�����ӣ�������ӵ��������" + mineMap[row][column]);
		if (mineMapOpened[row][column] == 1) {
			return;
		} else {
			mineMapOpened[row][column] = 1;
		}
		if (mineMap[row][column] == 1) {
			System.out.println("GameOver");
			gameOver();
		} else {
			int aroundMineNum = 0;
			if (row == 0) {
				if (column == 0) {
					// ���Ͻ�
					if (mineMap[row][column + 1] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row + 1][column] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row + 1][column + 1] == 1) {
						aroundMineNum++;
					}
					if (aroundMineNum == 0) {
						TextView view = (TextView) gameViewLayout
								.findViewById(id);
						view.setText("0");
						view.setBackgroundColor(Color.WHITE);
						openMap(id + 1);
						openMap(id + MAP_SIZE);
						openMap(id + (MAP_SIZE + 1));
					}
				} else if (column == MAP_SIZE - 1) {
					// ���Ͻ�
					if (mineMap[row][column - 1] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row + 1][column] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row + 1][column - 1] == 1) {
						aroundMineNum++;
					}
					if (aroundMineNum == 0) {
						TextView view = (TextView) gameViewLayout
								.findViewById(id);
						view.setText("0");
						view.setBackgroundColor(Color.WHITE);

						openMap(id - 1);
						openMap(id + MAP_SIZE);
						openMap(id + (MAP_SIZE - 1));
					}
				} else {
					// ��һ������
					if (mineMap[row][column - 1] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row][column + 1] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row + 1][column - 1] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row + 1][column] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row + 1][column + 1] == 1) {
						aroundMineNum++;
					}
					if (aroundMineNum == 0) {
						TextView view = (TextView) gameViewLayout
								.findViewById(id);
						view.setText("0");
						view.setBackgroundColor(Color.WHITE);

						openMap(id + 1);
						openMap(id + MAP_SIZE);
						openMap(id + (MAP_SIZE + 1));
						openMap(id - 1);
						openMap(id + (MAP_SIZE - 1));
					}
				}
			} else if (row == MAP_SIZE - 1) {
				if (column == 0) {
					// ���½�
					if (mineMap[row - 1][column] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row - 1][column + 1] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row][column + 1] == 1) {
						aroundMineNum++;
					}

					if (aroundMineNum == 0) {
						TextView view = (TextView) gameViewLayout
								.findViewById(id);
						view.setText("0");
						view.setBackgroundColor(Color.WHITE);

						openMap(id - MAP_SIZE);
						openMap(id - (MAP_SIZE - 1));
						openMap(id + 1);
					}
				} else if (column == MAP_SIZE - 1) {
					// ���½�
					if (mineMap[row - 1][column] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row - 1][column - 1] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row][column - 1] == 1) {
						aroundMineNum++;
					}
					if (aroundMineNum == 0) {
						TextView view = (TextView) gameViewLayout
								.findViewById(id);
						view.setText("0");
						view.setBackgroundColor(Color.WHITE);

						openMap(id - 1);
						openMap(id - MAP_SIZE);
						openMap(id - (MAP_SIZE + 1));
					}
				} else {
					// ���һ������
					if (mineMap[row - 1][column - 1] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row - 1][column] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row - 1][column + 1] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row][column - 1] == 1) {
						aroundMineNum++;
					}
					if (mineMap[row][column + 1] == 1) {
						aroundMineNum++;
					}
					if (aroundMineNum == 0) {
						TextView view = (TextView) gameViewLayout
								.findViewById(id);
						view.setText("0");
						view.setBackgroundColor(Color.WHITE);

						openMap(id - 1);
						openMap(id - MAP_SIZE);
						openMap(id - (MAP_SIZE - 1));
						openMap(id + 1);
						openMap(id - (MAP_SIZE + 1));
					}
				}
			} else if (column == 0) {
				// ��һ������
				if (mineMap[row - 1][column] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row + 1][column] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row - 1][column + 1] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row][column + 1] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row + 1][column + 1] == 1) {
					aroundMineNum++;
				}
				if (aroundMineNum == 0) {
					TextView view = (TextView) gameViewLayout.findViewById(id);
					view.setText("0");
					view.setBackgroundColor(Color.WHITE);
					openMap(id - MAP_SIZE);
					openMap(id - (MAP_SIZE - 1));
					openMap(id + 1);
					openMap(id + MAP_SIZE);
					openMap(id + (MAP_SIZE + 1));
				}
			} else if (column == MAP_SIZE - 1) {
				// ���һ������
				if (mineMap[row - 1][column - 1] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row - 1][column] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row][column - 1] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row + 1][column - 1] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row + 1][column] == 1) {
					aroundMineNum++;
				}
				if (aroundMineNum == 0) {
					TextView view = (TextView) gameViewLayout.findViewById(id);
					view.setText("0");
					view.setBackgroundColor(Color.WHITE);
					openMap(id - (MAP_SIZE + 1));
					openMap(id - MAP_SIZE);
					openMap(id - 1);
					openMap(id + (MAP_SIZE - 1));
					openMap(id + MAP_SIZE);
				}
			} else {
				// ʣ��
				if (mineMap[row - 1][column - 1] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row - 1][column] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row][column - 1] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row + 1][column - 1] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row + 1][column] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row + 1][column + 1] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row][column + 1] == 1) {
					aroundMineNum++;
				}
				if (mineMap[row - 1][column + 1] == 1) {
					aroundMineNum++;
				}
				if (aroundMineNum == 0) {
					TextView view = (TextView) gameViewLayout.findViewById(id);
					view.setText("0");
					view.setBackgroundColor(Color.WHITE);
					openMap(id - (MAP_SIZE + 1));
					openMap(id - MAP_SIZE);
					openMap(id - (MAP_SIZE - 1));
					openMap(id - 1);
					openMap(id + 1);
					openMap(id + (MAP_SIZE - 1));
					openMap(id + MAP_SIZE);
					openMap(id + (MAP_SIZE + 1));
				}
			}

			if (aroundMineNum != 0) {
				TextView view = (TextView) gameViewLayout.findViewById(id);
				view.setText("" + aroundMineNum);
				view.setBackgroundColor(Color.BLUE);
				view.setTextColor(Color.BLACK);
			}
		}
		mapUnOpenLeft--;
		if (mapUnOpenLeft <= MINES) {
			gameWin();
		}
	}

	public void gameOver() {
		for (int i = 0; i < mineIds.size(); i++) {
			TextView view = (TextView) gameViewLayout.findViewById(mineIds
					.get(i));
			if (isIdClear(mineIds.get(i))) {
				view.setText("��");
				view.setBackgroundColor(Color.GREEN);
				view.setTextColor(Color.BLACK);
			} else {
				view.setText("��");
				view.setBackgroundColor(Color.RED);
				view.setTextColor(Color.BLACK);
			}
		}
		isGameStart = false;
		showCusDialog("��ʾ", "�Բ������ȵ����ˣ���Ϸ����", "�ٴο�ʼ", "�˳���Ϸ",
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						isGameStart = false;
						initGame();
						initGameView();
					}
				}, new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
	}

	public void gameWin() {
		isGameStart = false;
		showCusDialog("��ʾ", "��ϲ���������ף���ʱ" + time + "��", "�ٴο�ʼ", "�˳���Ϸ",
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						initGame();
						initGameView();
					}
				}, new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
	}

	public void showCusDialog(String title, CharSequence msg,
			String btnConfirm, String btnCancel,
			final OnClickListener confirmOnClickListener,
			final OnClickListener cancelOnClickListener) {
		final Dialog dialog = new Dialog(context, R.style.CustomDialog);
		// ��������ContentView
		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(R.layout.view_custom_dialog);
		TextView tv_title = (TextView) dialog.findViewById(R.id.tv_title);
		tv_title.setText(Html.fromHtml(title));
		TextView tv_content = (TextView) dialog.findViewById(R.id.tv_content);
		tv_content.setText(msg);

		Button bt_confirm = (Button) dialog.findViewById(R.id.bt_confirm);
		Button bt_cancel = (Button) dialog.findViewById(R.id.bt_cancel);

		bt_confirm.setText(btnConfirm);
		bt_cancel.setText(btnCancel);

		bt_confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (confirmOnClickListener != null) {
					confirmOnClickListener.onClick(v);
				}
				dialog.dismiss();
			}
		});

		bt_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (cancelOnClickListener != null) {
					cancelOnClickListener.onClick(v);
				}
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private class MineOnclickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (!v.isClickable()) {
				return;
			}
			if (!isGameStart) {
				isGameStart = true;
				timeThread = new TimeThread();
				timeThread.start();
				btn_start.setText("����");
			}
			openMap(v.getId());
		}
	}

	private class MineOnLongClickListener implements OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			int id = v.getId();
			int row = id / MAP_SIZE;
			int column = id % MAP_SIZE;

			if (mineMapOpened[row][column] == 1) {
				return true;
			} else if (v.isClickable()) {
				if (mineClear >= MINES) {
					return true;
				}
				TextView view = (TextView) v;
				view.setText("X");
				view.setBackgroundColor(Color.YELLOW);
				view.setClickable(false);
				mineClear++;
				tv_mines.setText("����:" + (MINES - mineClear) + "��");
				mineClearId.add(id);
			} else {
				TextView view = (TextView) v;
				view.setText("*");
				view.setBackgroundColor(Color.GRAY);
				view.setClickable(true);
				mineClear--;
				tv_mines.setText("����:" + (MINES - mineClear) + "��");
				removeClearId(id);
			}
			return true;
		}
	}

	private void removeClearId(int id) {
		if (mineClearId != null && mineClearId.size() != 0) {
			for (int i = mineClearId.size() - 1; i >= 0; i--) {
				if (mineClearId.get(i) == id) {
					mineClearId.remove(i);
				}
			}
		}
	}

	public boolean isIdClear(int id) {
		if (mineClearId != null && mineClearId.size() != 0) {
			for (int i = 0; i < mineClearId.size(); i++) {
				if (id == mineClearId.get(i)) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressLint("HandlerLeak")
	private Handler timeHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (isGameStart) {
				time++;
			}
			tv_time.setText("ʱ�䣺" + time + "��");
		}

	};

	class TimeThread extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (isGameStart) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				timeHandler.sendEmptyMessage(0);
			}
		}

	}

}
