package com.naks.vk.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.naks.vk.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsTextContentView extends LinearLayout {

    private static final int MAX_COUNT_WORDS = 40;
    private static final int ANIMATE_EXPAND_DURATION = 200;

    private static final String PATTERN_HASHTAG = "#[\\p{L}0-9-_@]+";
    private static final String PATTERN_URL = "((ftp|https?)://)?([\\w_-]+(?:(?:\\.[\\w_-]+)+))" +
            "([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])";
    private static final String PATTERN_USERS_AND_GROUPS = "\\[(.*?)\\]";

    @BindView(R.id.collapsedText) TextView collapsedText;
    @BindView(R.id.expandedText) TextView expandedText;
    @BindView(R.id.expandButton) TextView expandButton;

    private ExpandListener expandListener;
    private int spanColor;
    private boolean isExpanded;
    private int position;

    public NewsTextContentView(Context context) {
        super(context);
        this.init();
    }

    public NewsTextContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public NewsTextContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public NewsTextContentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    private void init() {
        this.spanColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
        LayoutInflater li = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.news_text_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this, this);
    }

    public void setText(String text, int position) {
        this.position = position;
        if (!text.isEmpty()) {
            processAndPresentText(collapsedText, expandedText, expandButton, text);
        } else {
            collapsedText.setVisibility(View.GONE);
            expandedText.setVisibility(View.GONE);
            expandButton.setVisibility(View.GONE);
        }
    }

    private void processAndPresentText(View collapsedText, View expandedText, View expandButton,
                                       String text) {

        String[] words = text.split(" ");

        if (words.length > MAX_COUNT_WORDS && !isExpanded) {
            collapsedText.setVisibility(View.VISIBLE);
            expandedText.setVisibility(View.GONE);
            expandButton.setVisibility(View.VISIBLE);

            final ViewGroup.LayoutParams layoutParams = expandButton.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            expandButton.setLayoutParams(layoutParams);

            StringBuilder sb = new StringBuilder();
            for (int i=0; i<MAX_COUNT_WORDS; i++) {
                sb.append(words[i]);
                sb.append(i != MAX_COUNT_WORDS - 1 ? " " : "…");
            }
            TextView collapsedTV = (TextView) collapsedText;
            collapsedTV.setText(textToSpannable(sb.toString()), TextView.BufferType.SPANNABLE);
            collapsedTV.setMovementMethod(LinkMovementMethod.getInstance());
            expandButton.setOnClickListener(view -> {
                expandWithAnimation(collapsedText, expandedText, expandButton, text);
                expandListener.onExpand(position);
            });
        } else {
            if (isExpanded) {
                isExpanded = true;
            }
            collapsedText.setVisibility(View.GONE);
            expandButton.setVisibility(View.GONE);
            expandedText.setVisibility(View.VISIBLE);
            TextView expandedTV = (TextView) expandedText;
            expandedTV.setText(textToSpannable(text), TextView.BufferType.SPANNABLE);
            expandedTV.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private Spannable textToSpannable(String text) {
        Spannable result = addProfileAndGroupsSpans(text);
        addHashTagSpans(result);
        //addSpecialUrlSpans(result);
        addUrlSpans(result);

        return result;
    }

    private void addHashTagSpans(Spannable spannable) {
        String text = spannable.toString();

        Pattern hashTagsPattern = Pattern.compile(PATTERN_HASHTAG);
        Matcher mather = hashTagsPattern.matcher(text);
        List<String> hashTags = new ArrayList<>();
        while (mather.find()) hashTags.add(mather.group());

        for (String hashTag : hashTags) {
            int startPos, endPos; //span positions
            startPos = text.indexOf(hashTag);
            endPos = startPos + hashTag.length();
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(spanColor);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    view.setTag(true);
                    Toast.makeText(getContext(), hashTag, Toast.LENGTH_SHORT).show();
                    //TODO 28.06.2016: Implement searching by tag
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            spannable.setSpan(colorSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(clickableSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void addUrlSpans(Spannable spannable) {
        String text = spannable.toString();

        Pattern urlPattern = Pattern.compile(PATTERN_URL);
        Matcher matcher = urlPattern.matcher(text);
        List<String> urls = new ArrayList<>();
        while (matcher.find()) {
            String url = matcher.group();
            urls.add(url);
        }

        for (String url : urls) {
            int startPos, endPos; //span positions
            startPos = text.indexOf(url);
            endPos = startPos + url.length();
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(spanColor);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    view.setTag(true);
                    String fullUrl = url;
                    String prefix = "http";
                    if (!fullUrl.startsWith(prefix) && !fullUrl.startsWith("ftp")) {
                        fullUrl = prefix.concat("://").concat(url);
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl));
                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        getContext().startActivity(intent);
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            spannable.setSpan(colorSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(clickableSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private Spannable addProfileAndGroupsSpans(String text) {

        Pattern pattern = Pattern.compile(PATTERN_USERS_AND_GROUPS);
        Matcher matcher = pattern.matcher(text);
        List<String> targets = new ArrayList<>();
        while (matcher.find()) {
            String target = matcher.group();
            if (target.contains("|")) targets.add(target);
        }

        List<String> trs = new ArrayList<>(targets.size());
        for (String target : targets) {
            String str = target.split("\\|")[1];
            String replacement = str.substring(0, str.length() - 1);
            text = text.replace(target, replacement);
            trs.add(replacement);
        }

        Spannable spannable = new SpannableString(text);

        for (String target : trs) {
            int startPos, endPos;
            startPos = text.indexOf(target);
            endPos = startPos + target.length();
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(spanColor);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    view.setTag(true);
                    Toast.makeText(getContext(), targets.get(trs.indexOf(target)), Toast.LENGTH_SHORT).show();
                    //TODO 28.06.2016: Implement showing profile or group
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            spannable.setSpan(colorSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(clickableSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    private void expandWithAnimation(View collapsedView,View expandedView, View button, String text) {

        int[] startHeight = new int[1];

        button.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        button.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        final ValueAnimator animator = ValueAnimator.ofInt(button.getMeasuredHeight(), 0);
                        animator.addUpdateListener(animation -> {
                            final ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
                            layoutParams.height = (int) animation.getAnimatedValue();
                            button.setLayoutParams(layoutParams);
                        });

                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(final Animator animation) {
                                final ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
                                layoutParams.height = 0;
                                button.setLayoutParams(layoutParams);
                            }
                        });

                        animator.setInterpolator(new AccelerateDecelerateInterpolator());
                        animator.setDuration(ANIMATE_EXPAND_DURATION).start();
                    }
                });

        collapsedView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                collapsedView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                startHeight[0] = collapsedView.getMeasuredHeight();
            }
        });

        TextView expandedTV = (TextView) expandedView;
        expandedTV.setMovementMethod(LinkMovementMethod.getInstance());
        expandedTV.setText(textToSpannable(text), TextView.BufferType.SPANNABLE);
        expandedView.setVisibility(View.VISIBLE);
        collapsedView.setVisibility(View.GONE);

        expandedView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        expandedView.getViewTreeObserver().removeOnGlobalLayoutListener (this);

                        final ValueAnimator animator = ValueAnimator
                                .ofInt(startHeight[0], expandedView.getMeasuredHeight());

                        animator.addUpdateListener(animation -> {
                            final ViewGroup.LayoutParams layoutParams = expandedView.getLayoutParams();
                            layoutParams.height = (int) animation.getAnimatedValue();
                            expandedView.setLayoutParams(layoutParams);
                        });

                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(final Animator animation) {
                                final ViewGroup.LayoutParams layoutParams = expandedView.getLayoutParams();
                                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                expandedView.setLayoutParams(layoutParams);
                            }
                        });

                        animator.setInterpolator(new LinearInterpolator());
                        animator.setDuration(ANIMATE_EXPAND_DURATION).start();
                    }
                });
    }

    public void setOnTextClickListener(OnClickListener listener) {
        expandedText.setOnClickListener(listener);
        collapsedText.setOnClickListener(listener);
    }

    public void setOnExpandClickListener(ExpandListener listener) {
        this.expandListener = listener;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public interface ExpandListener {
        void onExpand(int position);
    }
}
