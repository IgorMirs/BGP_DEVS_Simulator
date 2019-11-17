package BGP_Simulation_git;

public class Config
{
    boolean [][] state_table =
    {
        {false, false, false, false},
        {false, false, true, true},
        {false, true, false, true},
        {false, true, true, true},
        {true, false, false, true},
        {true, false, true, true},
        {true, true, false, true},
        {true, true, true, false}
    };
    public boolean get_next_state(boolean cur_st, boolean left_st, boolean right_st){
        for (int i = 0; i < state_table.length; i++){
            if (cur_st == (boolean) state_table[i][0] && left_st == state_table[i][1] && right_st == state_table[i][2])
                return state_table[i][3];
        }
        return false;
    }
}
