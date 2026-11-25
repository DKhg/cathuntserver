package com.hong.cathuntserver.play;

import java.util.List;

// 레벨·경험치 계산만 하는 순수 로직(스프링·DB 의존 없음). Unity 클라의 Leveling과 같은 규칙으로 맞췄다.
public final class Leveling {

    private Leveling() { }

    // 레벨업 결과: 올라간 레벨과, 다음 레벨로 넘기는 남은 경험치.
    public record Progress(int level, long exp) { }

    // 지금 레벨에서 다음 레벨로 가는 데 필요한 경험치. 표 끝을 넘으면 마지막 값을 쓰고, 표가 비면 100으로 본다.
    public static int expToNext(int level, List<Integer> table) {
        if (table == null || table.isEmpty()) return 100;
        int index = level - 1;                       // Lv.1 → 표의 0번
        if (index < 0) index = 0;
        if (index >= table.size()) index = table.size() - 1;
        int need = table.get(index);
        return need < 1 ? 1 : need;                  // 0 이하 방어(무한 루프 방지)
    }

    // 경험치가 쌓인 상태를 받아, 넘치는 만큼 레벨을 올려서 돌려준다(한 번에 여러 레벨 가능).
    public static Progress apply(int level, long exp, List<Integer> table) {
        if (level < 1) level = 1;
        if (exp < 0) exp = 0;
        int need = expToNext(level, table);
        while (exp >= need) {        // 다음 레벨 요구치를 채웠으면
            exp -= need;             // 그만큼 쓰고
            level++;                 // 레벨을 올리고
            need = expToNext(level, table);
        }
        return new Progress(level, exp);
    }
}
