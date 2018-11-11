package com.example.hg_bar;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class AddNewDrink extends ActionBarActivity {
    DBHandler dbHandler;
    ListView bottleView;

    ArrayAdapter<String> Adapter;
    Drink D;
    Button button_add, button_delete, button_add_bottles;
    ArrayList<String> bottles = new ArrayList<String>();
    EditText edit_name;
    TextView edit_bottles;
    EditText edit_amounts1, edit_amounts2, edit_amounts3, edit_amounts4, edit_amounts5;
    String name, amounts;
    CheckBox check_box_add_week, check_box_soda, check_box_milk, check_box_juice, check_box_free;
    int count = 0;
    int nr = 0;
    String soda = "S";
    String milk = "M";
    String juice = "J";
    String free = "F";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_drink);
        dbHandler = DBHandler.getInstance(getApplicationContext());

        bottleView = (ListView) findViewById(R.id.bottleView);

        bottleView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        printBottles();

        button_add_bottles = (Button) findViewById(R.id.button_add_bottles);
        button_add = (Button) findViewById(R.id.button_add);
        button_delete = (Button) findViewById(R.id.button_delete);

        check_box_add_week = (CheckBox) findViewById(R.id.check_box_add_week);
        check_box_soda = (CheckBox) findViewById(R.id.check_box_soda);
        check_box_milk = (CheckBox) findViewById(R.id.check_box_milk);
        check_box_juice = (CheckBox) findViewById(R.id.check_box_juice);
        check_box_free = (CheckBox) findViewById(R.id.check_box_free);

        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_bottles = (TextView) findViewById(R.id.edit_bottles);
        edit_amounts1 = (EditText) findViewById(R.id.edit_amounts1);
        edit_amounts2 = (EditText) findViewById(R.id.edit_amounts2);
        edit_amounts3 = (EditText) findViewById(R.id.edit_amounts3);
        edit_amounts4 = (EditText) findViewById(R.id.edit_amounts4);
        edit_amounts5 = (EditText) findViewById(R.id.edit_amounts5);

        edit_amounts1.setVisibility(View.INVISIBLE);
        edit_amounts2.setVisibility(View.INVISIBLE);
        edit_amounts3.setVisibility(View.INVISIBLE);
        edit_amounts4.setVisibility(View.INVISIBLE);
        edit_amounts5.setVisibility(View.INVISIBLE);

        edit_name.setText("");
        edit_bottles.setText("");


        button_add.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        name = edit_name.getText().toString();
                        amounts = edit_amounts1.getText().toString();
                        String info = "";
                        if (setAmoutField(nr) == nr && !checkType().equals("null")) {
                            String[] tokens = amounts.split("\\s+");
                            String tmp = "";
                            for (int i = 0; i < tokens.length; ++i) {
                                tmp += tokens[i] + "\n";

                            }
                            String type = checkType();
                          //  D = new Drink(name, bottles, tmp, count, info, type);
                           // dbHandler.addDrinks(D);

                            if (check_box_add_week.isChecked()) {
                                dbHandler.setQuickChoice(name, 1);
                            }
                            bottles.clear();
                            edit_name.setText("");
                            edit_bottles.setText("");
                            printBottles();
                            nr = 0;
                            setAmoutField(nr);
                        }


                    }
                }
        );
        button_delete.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {


                    }
                }
        );

        button_add_bottles.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        SparseBooleanArray checked = bottleView.getCheckedItemPositions();
                        System.out.println("----1 ");
                        ArrayList<String> bottles_tmp = new ArrayList<String>();
                        String bottles_text = "";

                        for (int i = 0; i < checked.size(); i++) {
                            int pos = checked.keyAt(i);
                            if (checked.valueAt(i) && Adapter.getItem(pos) != null) {
                                bottles_text += Adapter.getItem(pos) + " ";
                                bottles_tmp.add(Adapter.getItem(pos));
                                bottles = bottles_tmp;
                                nr = bottles.size();
                                edit_bottles.setText(bottles_text);
                            }

                        }
                        setAmoutField(nr);

                    }
                }
        );
    }

    public int setAmoutField(int nr) {
        if(nr ==0) {
            edit_amounts1.setVisibility(View.INVISIBLE);
            edit_amounts2.setVisibility(View.INVISIBLE);
            edit_amounts3.setVisibility(View.INVISIBLE);
            edit_amounts4.setVisibility(View.INVISIBLE);
            edit_amounts5.setVisibility(View.INVISIBLE);
        }

        else if (nr == 1) {
            edit_amounts1.setVisibility(View.VISIBLE);
            edit_amounts2.setVisibility(View.INVISIBLE);
            edit_amounts3.setVisibility(View.INVISIBLE);
            edit_amounts4.setVisibility(View.INVISIBLE);
            edit_amounts5.setVisibility(View.INVISIBLE);
            return 1;
        } else if (nr == 2) {
            edit_amounts1.setVisibility(View.VISIBLE);
            edit_amounts2.setVisibility(View.VISIBLE);
            edit_amounts3.setVisibility(View.INVISIBLE);
            edit_amounts4.setVisibility(View.INVISIBLE);
            edit_amounts5.setVisibility(View.INVISIBLE);
            return 2;
        } else if (nr == 3) {
            edit_amounts1.setVisibility(View.VISIBLE);
            edit_amounts2.setVisibility(View.VISIBLE);
            edit_amounts3.setVisibility(View.VISIBLE);
            edit_amounts4.setVisibility(View.INVISIBLE);
            edit_amounts5.setVisibility(View.INVISIBLE);
            return 3;
        } else if (nr == 4) {
            edit_amounts1.setVisibility(View.VISIBLE);
            edit_amounts2.setVisibility(View.VISIBLE);
            edit_amounts3.setVisibility(View.VISIBLE);
            edit_amounts4.setVisibility(View.VISIBLE);
            edit_amounts5.setVisibility(View.INVISIBLE);
            return 4;

        } else if (nr == 5) {
            edit_amounts1.setVisibility(View.VISIBLE);
            edit_amounts2.setVisibility(View.VISIBLE);
            edit_amounts3.setVisibility(View.VISIBLE);
            edit_amounts4.setVisibility(View.VISIBLE);
            edit_amounts5.setVisibility(View.VISIBLE);
            return 5;
        }
        return 0;
    }

    public String checkType() {
        if (check_box_soda.isChecked())
            return soda;
        else if (check_box_milk.isChecked())
            return milk;
        else if (check_box_juice.isChecked())
            return juice;
        else if (check_box_free.isChecked())
            return free;
        else {
            Toast msg = Toast.makeText(getBaseContext(),
                    "Välj en typ.", Toast.LENGTH_LONG);
            msg.show();
            return "null";
        }

    }
/*
    boolean check(String amounts) {
        String[] tokens = amounts.split("\\s+");
        if (nr == tokens.length)
            return true;
        else {
            Toast msg = Toast.makeText(getBaseContext(),
                    "För få mängder.", Toast.LENGTH_LONG);
            msg.show();
            return false;
        }

    }
*/
    void printBottles() {
        ArrayList<String> bottles = dbHandler.bottlesToString();

        Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, bottles);
        bottleView.setAdapter(Adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new_drink, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
