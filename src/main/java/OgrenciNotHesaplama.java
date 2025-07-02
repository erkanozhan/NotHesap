import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class OgrenciNotHesaplama extends JFrame {
    private JTextField alanCalismasiField, araSinavField, finalField;
    private JTextField araSinavOranField, alanOranField, finalOranField;
    private JLabel ortalamaLabel, gerekliFinalLabel, eklenecekPuanLabel, yuvarlanmisFinalLabel;
    private JButton hesaplaButton, temizleButton;

    public OgrenciNotHesaplama() {
        setTitle("Öğrenci Not Hesaplama (Yuvarlamalı)");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(11, 2, 10, 10));

        // Oran giriş alanları (tıklayınca temizlenmez)
        araSinavOranField = createNonClearableField("30");
        alanOranField = createNonClearableField("10");
        finalOranField = createNonClearableField("60");

        // Not giriş alanları (tıklayınca temizlenir)
        alanCalismasiField = createClearableField("Alan Çalışması Notu");
        araSinavField = createClearableField("Ara Sınav Notu");
        finalField = createClearableField("Final Notu");

        // Sonuç alanları
        ortalamaLabel = createResultLabel("0", Color.BLACK);
        gerekliFinalLabel = createResultLabel("-", Color.BLACK);
        yuvarlanmisFinalLabel = createResultLabel("-", new Color(0, 100, 0));
        eklenecekPuanLabel = createResultLabel("-", Color.RED);

        // Butonlar
        hesaplaButton = new JButton("Hesapla");
        temizleButton = new JButton("Temizle");

        // UI yerleşimi
        addComponents();

        // Olay dinleyicileri
        setupEventListeners();
    }

    private JTextField createNonClearableField(String defaultValue) {
        JTextField field = new JTextField(defaultValue);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBackground(Color.WHITE);
        return field;
    }

    private JTextField createClearableField(String hint) {
        JTextField field = new JTextField(hint);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setForeground(Color.GRAY);

        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (field.getText().equals(hint)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
        });

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(hint)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
                field.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(hint);
                }
            }
        });

        return field;
    }

    private JLabel createResultLabel(String text, Color color) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setForeground(color);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    private void addComponents() {
        // Oranlar
        add(new JLabel("Ara Sınav Oranı (%):"));
        add(araSinavOranField);
        add(new JLabel("Alan Çalışması Oranı (%):"));
        add(alanOranField);
        add(new JLabel("Final Oranı (%):"));
        add(finalOranField);

        // Notlar (istenen sırada)
        add(new JLabel("Alan Çalışması Notu:"));
        add(alanCalismasiField);
        add(new JLabel("Ara Sınav Notu:"));
        add(araSinavField);
        add(new JLabel("Final Notu:"));
        add(finalField);

        // Sonuçlar
        add(new JLabel("Ortalama:"));
        add(ortalamaLabel);
        add(new JLabel("Gerekli Final (Tam):"));
        add(gerekliFinalLabel);
        add(new JLabel("Gerekli Final (Yuvarlanmış):"));
        add(yuvarlanmisFinalLabel);
        add(new JLabel("Eklenecek Puan:"));
        add(eklenecekPuanLabel);

        // Butonlar
        add(hesaplaButton);
        add(temizleButton);
    }

    private void setupEventListeners() {
        hesaplaButton.addActionListener(e -> hesaplaVeGoster());
        temizleButton.addActionListener(e -> temizle());

        // Enter tuşu ile hesaplama
        ActionListener calculateAction = e -> hesaplaVeGoster();
        alanCalismasiField.addActionListener(calculateAction);
        araSinavField.addActionListener(calculateAction);
        finalField.addActionListener(calculateAction);
    }

    private void hesaplaVeGoster() {
        try {
            // Oranları al
            double araSinavOran = Double.parseDouble(araSinavOranField.getText());
            double alanOran = Double.parseDouble(alanOranField.getText());
            double finalOran = Double.parseDouble(finalOranField.getText());

            // Notları al (ipuçları varsa 0 olarak kabul et)
            double alanCalismasi = getFieldValue(alanCalismasiField, "Alan Çalışması Notu");
            double araSinav = getFieldValue(araSinavField, "Ara Sınav Notu");
            double finalNotu = getFieldValue(finalField, "Final Notu");

            // Kontroller
            if (!oranlariKontrolEt(araSinavOran, alanOran, finalOran)) return;
            if (!notlariKontrolEt(alanCalismasi, araSinav, finalNotu)) return;

            // Ortalama hesapla
            double ortalama = hesaplaOrtalama(alanCalismasi, araSinav, finalNotu, alanOran, araSinavOran, finalOran);
            ortalamaLabel.setText(String.format("%.2f", ortalama));

            // Geçme durumunu kontrol et
            if (ortalama >= 60) {
                gerekliFinalLabel.setText("Zaten geçiyor");
                yuvarlanmisFinalLabel.setText("-");
                eklenecekPuanLabel.setText("0");
                eklenecekPuanLabel.setForeground(Color.BLACK);
                return;
            }

            // Minimum gereken final notunu hesapla
            double minFinal = hesaplaMinimumFinal(alanCalismasi, araSinav, alanOran, araSinavOran, finalOran);
            gerekliFinalLabel.setText(String.format("%.2f", minFinal));

            // Yuvarlanmış final notunu hesapla
            double yuvarlanmisFinal = yuvarlaStandard(minFinal);
            yuvarlanmisFinalLabel.setText(String.format("%.0f", yuvarlanmisFinal));

            // Eklenecek puanı hesapla ve yuvarla
            double eklenecekPuan = yuvarlaStandard(minFinal - finalNotu);
            if (eklenecekPuan > 0) {
                eklenecekPuanLabel.setText(String.format("+%.0f", eklenecekPuan));
                eklenecekPuanLabel.setForeground(Color.RED);
            } else {
                eklenecekPuanLabel.setText("0");
                eklenecekPuanLabel.setForeground(Color.BLACK);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli sayılar giriniz!", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double getFieldValue(JTextField field, String hint) {
        if (field.getText().equals(hint) || field.getText().isEmpty()) {
            return 0;
        }
        return Double.parseDouble(field.getText());
    }

    private double hesaplaMinimumFinal(double alanCalismasi, double araSinav,
                                       double alanOran, double araSinavOran, double finalOran) {
        double gerekliFinal = (59.5 - (alanCalismasi * alanOran + araSinav * araSinavOran)/100) / (finalOran/100);
        return Math.max(0, Math.min(100, gerekliFinal));
    }

    private double hesaplaOrtalama(double alanCalismasi, double araSinav, double finalNotu,
                                   double alanOran, double araSinavOran, double finalOran) {
        double ortalama = (alanCalismasi * alanOran + araSinav * araSinavOran + finalNotu * finalOran) / 100;
        return yuvarlaBuçukUste(ortalama);
    }

    private double yuvarlaBuçukUste(double sayi) {
        return BigDecimal.valueOf(sayi).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private double yuvarlaStandard(double sayi) {
        return BigDecimal.valueOf(sayi).setScale(0, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean oranlariKontrolEt(double araSinavOran, double alanOran, double finalOran) {
        double toplamOran = araSinavOran + alanOran + finalOran;
        if (Math.abs(toplamOran - 100) > 0.001) {
            JOptionPane.showMessageDialog(this,
                    "Oranların toplamı 100 olmalıdır! (Toplam: " + toplamOran + ")",
                    "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean notlariKontrolEt(double alanCalismasi, double araSinav, double finalNotu) {
        if (alanCalismasi < 0 || alanCalismasi > 100 ||
                araSinav < 0 || araSinav > 100 ||
                finalNotu < 0 || finalNotu > 100) {
            JOptionPane.showMessageDialog(this,
                    "Notlar 0-100 arasında olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void temizle() {
        alanCalismasiField.setText("Alan Çalışması Notu");
        alanCalismasiField.setForeground(Color.GRAY);
        araSinavField.setText("Ara Sınav Notu");
        araSinavField.setForeground(Color.GRAY);
        finalField.setText("Final Notu");
        finalField.setForeground(Color.GRAY);
        ortalamaLabel.setText("0");
        gerekliFinalLabel.setText("-");
        yuvarlanmisFinalLabel.setText("-");
        eklenecekPuanLabel.setText("-");
        eklenecekPuanLabel.setForeground(Color.BLACK);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OgrenciNotHesaplama frame = new OgrenciNotHesaplama();
            frame.setVisible(true);
        });
    }
}