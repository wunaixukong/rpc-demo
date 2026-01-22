package tech.insight.rpc;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Daily {


    public static void main(String[] args) {

        // 19 - 16 = 3 有16?  记 1
        // 3  有8? 记 0
        // 3  有4? 记0
        // 3 -2 = 1  有2? 记1
        // 1 -1 = 0 有1 记1
        // 10011

        // 25
        // 25 - 16 = 9 有16? 记 1
        // 9 - 8 = 1 有8 记1
        // 1 有4? 记0
        // 1 有2? 记0
        // 1-1=0 有1 记1
        // 11001

        // 1000=8 10000=16 100000=32 1000000=64
        // 50 -> ? 6位数字
        // 50 - 32 = 18 > 1
        // 18 - 16 = 2 > 1
        // 2 < 8 ? > 0
        // 2 < 4 ? > 0
        // 2 - 2 = 0 > 1
        // 0?
        // 11001

//      x=7  0111 = 7
//  反码 ~x   1000
//  补码 -x   1001 = -7
//           1000 = 8
//        System.out.println(36 | 37);
//        minBitwiseArray(Arrays.asList(11,13,31));
//        System.out.println(climbStairs(6));

        System.out.println(isIsomorphic("foo", "bar"));
    }

    /**
     * 给定两个字符串 s 和 t ，判断它们是否是同构的。
     *
     * 如果 s 中的字符可以按某种映射关系替换得到 t ，那么这两个字符串是同构的。
     *
     * 每个出现的字符都应当映射到另一个字符，同时不改变字符的顺序。
     * 不同字符不能映射到同一个字符上，相同字符只能映射到同一个字符上，字符可以映射到自己本身。
     * @param s
     * @param t
     * @return
     */
    public static boolean isIsomorphic(String s, String t) {
        //aaabbbba , bbbaaaba
        HashMap<Character, Character> st = new HashMap<>(), ts = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            char l = s.charAt(i);
            char m = t.charAt(i);

            // 如果我存过l, 那么l一定等于m
            // add egg , 如果是l=d,那么m=g
            if (st.containsKey(l) && st.get(l) != m ||
                ts.containsKey(m) && ts.get(m) != l) {
                return false;
            }
            st.put(l, m);
            ts.put(m, l);
        }
        return true;
    }

    /**
     * 假设你正在爬楼梯。需要 n 阶你才能到达楼顶。
     *
     * 每次你可以爬 1 或 2 个台阶。你有多少种不同的方法可以爬到楼顶呢？
     * @param n
     * @return
     */
    public static int climbStairs(int n) {
        // n = 1
        // ans[0] = 1; ans[1] = 1;
        // n = 2
        // ans[1] = 1; ans[2] = 1 + ans[1]; 1 + 1 = 2
        // n =3
        // ans[2] = 2; ans[3] = (1,1,1),(1,2),(2,1) = 3
        // n =4
        // ans[3] = 3 ; ans[4]= (1,1,1,1),(1,1,2),(1,2,1),(2,1,1),(2,2) = 5
        // n =5
        // ans[4] = 5 ; ans[5] =(1,1,1,1,1),(1,1,1,2),(1,1,2,1),(1,2,1,1),(2,1,1,1),(2,2,1),(2,1,1),(1,2,2) = 8
        // n =6
        // ans[5] = 8 ; ans[6] =(1,1,1,1,1,1),(1,1,1,1,2),(1,1,1,2,1),(1,1,2,1,1),(1,2,1,1,1),(2,1,1,1,1),(2,2,1,1),(2,1,2,1),(2,1,1,2),(1,2,2,1),(1,1,2,2),(1,2,1,2),(2,2,2)
        // ans[i] = ans[i-1] + ans[i-2]
        int[] ans = new int[n + 1];
        ans[0] = 1;
        ans[1] = 1;

        for (int i = 2; i <= n; i++) {
            ans[i] = ans[i - 1] + ans[i - 2];
        }
        return ans[n];
    }

    public static int[] minBitwiseArray(List<Integer> nums) {
        // 37
        // 37 - 32 = 5 > 1
        // 5 - 16 ? > 0
        // 5 - 8 ? > 0
        // 5 - 4 = 1? > 1
        // 1 - 2 ? > 0
        // 1 - 1 > 1
        // 100101 -> 100100 = 4 + 32 = 36
        int[] ans = new int[nums.size()];

        for (int i = 0; i < nums.size(); i++) {
            Integer num = nums.get(i);
            // 4 -> 100
            //  100
            //& 001
            //  000
            if ((num & 1) == 0){
                ans[i] = -1;
                continue;
            }
            // num = 7 -> 111
            // 0111 -> 0011
            // 找到连续的1
            // 连续的最高位
            //  00001000
            //& 11111000
            //      1000 >>

            //  0100
            //^ 0111
            //  0011 3


            int lowestZeroBit = (num + 1) & -(num + 1);
            ans[i] =  (lowestZeroBit >> 1) ^ num;

            // num = 13 1101
            // num + 1 = 1110
            //  1110
            //& 0010
            //  0010 lowest=2
            // lowest >> 1
            //  0001 = 1
            //^ 1101
            //  1100 = 12


            // num = 11 1011
            // num + 1 = 1100
            //  1100
            //& 0100
            //  0100
            // num >> 1
            //  0010
            //^ 1011
            //  1001 9


        }

        return ans;
    }

    public boolean possibleBipartition(int n, int[][] dislikes) {
        List<Integer>[] adj = new ArrayList[n + 1];
        for (int i = 1; i <= n; i++) {
            adj[i] = new ArrayList<>();
        }
        // 这里的e只有两个元素,1和2 并且1讨厌2
        // e:{1,2}
        for (int[] e : dislikes) {
            // 这里就相当于,在这个数组集合中添加了两个元素.
            // 在adj[1].add(2); adj[2].add(1); 就是两个集合里面分别add了对方讨厌的编号
            adj[e[0]].add(e[1]);
            adj[e[1]].add(e[0]);
        }
        int[] color = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            if (color[i] == 0) {
                LinkedList<Integer> queue = new LinkedList<>();
                queue.offer(i);
                color[i] = 1;

                while (!queue.isEmpty()) {
                    // 去除当前这个人 1
                    Integer curr = queue.poll();
                    for (Integer hate : adj[curr]) {
                        // 判断当前这个人跟你讨厌的人的颜色是否一样
                        if (color[hate] == color[curr]) {
                            // 是一样的直接返回false,这群人不能分为两拨
                            return false;
                        }

                        // 如果等于0说明没被染色
                        if (color[hate] == 0) {
                            color[hate] = -color[curr];
                            queue.offer(hate);
                        }
                    }
                    
                }
            }

        }


        return true;
    }
}
