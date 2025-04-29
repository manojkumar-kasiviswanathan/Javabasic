
    private static void printMessage(String message, String level) {
        try {
            BaseBDD.getScenario()
                    .ifPresent(
                            s -> {
                                String messageString =
                                        "\n========== "
                                                + level
                                                + " Start ==========\n"
                                                + message
                                                + "\n========== "
                                                + level
                                                + " End ============\n";

                                s.log(messageString);
                            });
        } catch (IllegalStateException e) {
            System.out.println("========== " + level + " Start ==========");
            System.out.println(message);
            System.out.println("========== " + level + " End ============");
        }
    }
