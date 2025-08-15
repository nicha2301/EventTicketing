"use client";

import { Component, ReactNode, ErrorInfo } from "react";
import { Button } from "@/components/ui/button";

interface Props {
  children?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export default class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Uncaught error:", error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="container-page flex min-h-[400px] flex-col items-center justify-center text-center">
          <h2 className="text-2xl font-semibold text-slate-900">Oops! Có lỗi xảy ra</h2>
          <p className="mt-2 text-slate-600">
            {this.state.error?.message || "Đã xảy ra lỗi không mong muốn"}
          </p>
          <Button
            onClick={() => this.setState({ hasError: false, error: undefined })}
            className="mt-4"
          >
            Thử lại
          </Button>
        </div>
      );
    }

    return this.props.children;
  }
}
